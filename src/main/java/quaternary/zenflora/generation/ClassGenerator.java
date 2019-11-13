package quaternary.zenflora.generation;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import quaternary.zenflora.IFlower;
import quaternary.zenflora.ZenFlora;
import quaternary.zenflora.annotation.Extends;
import quaternary.zenflora.annotation.Mirrors;
import quaternary.zenflora.generated.GeneratedClassSupport;
import quaternary.zenflora.templates.GenericFlowerTemplate;
import vazkii.botania.api.subtile.SubTileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassGenerator implements Opcodes {
	private static final String TEMPLATE_FIELD_NAME = "template";
	private static final String MINI_FIELD_NAME = "isMini";
	
	public static Class<? extends SubTileEntity> doIt(GenericFlowerTemplate flowerTemplate, String name, boolean mini) {
		ClassNode gen = new ClassNode(Opcodes.ASM5);
		Class<?> templateClass = flowerTemplate.getClass();
		Class<?> targetSuperclass = templateClass.getAnnotation(Extends.class).value();
		
		gen.name = "quaternary/zenflora/generated/" + name;
		gen.superName = Type.getInternalName(targetSuperclass);
		gen.version = V1_8; //Java 8
		gen.access = ACC_PUBLIC | ACC_SYNTHETIC;
		
		//Generate a field to store the flower template in
		FieldNode genTemplateField = new FieldNode(
			ASM5,
			ACC_PRIVATE | ACC_FINAL,
			TEMPLATE_FIELD_NAME,
			Type.getDescriptor(templateClass),
			null, null
		);
		gen.fields.add(genTemplateField);
		
		//And a field to store the mini-ness of the flower in.
		//Pass in the mininess now since I already know it.
		FieldNode genMiniField = new FieldNode(
			ASM5,
			ACC_PRIVATE | ACC_FINAL,
			MINI_FIELD_NAME,
			Type.BOOLEAN_TYPE.getDescriptor(),
			null,
			mini
		);
		gen.fields.add(genMiniField);
		
		//Generate a no-arg public constructor.
		//This fills in the "template" field by hitting up GeneratedClassSupport.
		MethodNode genConstructor = new MethodNode(ASM5, ACC_PUBLIC, "<init>", "()V", null, null);
		InsnList consInsns = new InsnList();
		
		//Call super
		consInsns.add(new VarInsnNode(ALOAD, 0));
		consInsns.add(new MethodInsnNode(
			INVOKESPECIAL,
			Type.getInternalName(targetSuperclass),
			"<init>",
			"()V", //Bankingo n it having a no-arguments constructor
			false
		));
		
		//Load template
		consInsns.add(new VarInsnNode(ALOAD, 0)); //this (for putfield later)
		consInsns.add(new LdcInsnNode(name)); //template name
		consInsns.add(new MethodInsnNode(
			INVOKESTATIC,
			Type.getInternalName(GeneratedClassSupport.class),
			"retrieveTemplate",
			Type.getMethodDescriptor(Type.getType(templateClass), Type.getType(String.class)),
			false
		));
		consInsns.add(new FieldInsnNode(
			PUTFIELD,
			gen.name,
			TEMPLATE_FIELD_NAME,
			Type.getDescriptor(templateClass)
		));
		
		//Also, while we're here, say hi to the console!
		consInsns.add(new FieldInsnNode(
			GETSTATIC,
			Type.getInternalName(ZenFlora.class),
			"LOGGER",
			Type.getDescriptor(Logger.class)
		));
		consInsns.add(new LdcInsnNode("Hello from " + name + "'s generated constructor!"));
		consInsns.add(new MethodInsnNode(
			INVOKEINTERFACE,
			Type.getInternalName(Logger.class),
			"info",
			Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),
			true
		));
		
		consInsns.add(new InsnNode(RETURN));
		
		genConstructor.instructions.add(consInsns);
		gen.methods.add(genConstructor);
		
		//For each non-null field in the template...
		try {
			for(Field templateField : flowerTemplate.getClass().getFields()) { //getFields respects inheritance
				if(templateField.get(flowerTemplate) == null) continue;
				
				//find out what method it's supposed to implement in the target superclass
				Mirrors mirrorAnnotation = templateField.getAnnotation(Mirrors.class);
				if(mirrorAnnotation == null) continue;
				
				String mirrorName = mirrorAnnotation.value();
				if(mirrorName.isEmpty()) mirrorName = templateField.getName(); //"" is the default in the annotation
				
				Method mirror = null;
				for(Method mirrorCandidate : targetSuperclass.getMethods()) { //also respects inheritance
					if(mirrorCandidate.getName().equals(mirrorName)) {
						//Banking on there being no two methods with the same name but different signatures
						mirror = mirrorCandidate;
						break;
					}
				}
				
				if(mirror == null) throw new IllegalStateException("Can't find mirror for template field " + templateField.getName());
				
				//Btw, it's always a functional interface, so I can get away with this.
				Method templateFunction = templateField.getType().getDeclaredMethods()[0];
				Type templateFunctionType = Type.getType(templateFunction);
				Type templateFunctionReturnType = Type.getReturnType(templateFunction);
				//And since it's always the same functional interface structure I can even get away with this!
				Type simulatedSuperFunctionType = templateFunctionType.getArgumentTypes()[1];
				
				//On to the method generation.
				MethodNode genMirrorOverride = new MethodNode(
					ASM5,
					ACC_PUBLIC,
					mirror.getName(),
					Type.getMethodDescriptor(mirror),
					null, //Banking on nothing needing a signature
					null  //Banking on nothing needing a throws clause
				);
				
				//Generate the method body of the override
				InsnList body = new InsnList();
				
				//First grab the zenscript callable
				body.add(new VarInsnNode(ALOAD, 0));  //this
				body.add(new FieldInsnNode(
					GETFIELD,
					gen.name,
					TEMPLATE_FIELD_NAME,
					Type.getDescriptor(templateClass)  //.template
				));
				body.add(new FieldInsnNode(
					GETFIELD,
					Type.getInternalName(templateClass),
					templateField.getName(),
					Type.getDescriptor(templateField.getType())//.whateverMethod
				));
				
				//Start filling out the arguments
				//IFlower is one. Hey... I'm an IFlower
				body.add(new VarInsnNode(ALOAD, 0));
				
				//Super method is next
				//TODO: generate the INVOKEDYNAMIC with the super method (sounds like fun)
				body.add(new InsnNode(ACONST_NULL)); //push null for now... LOL
				
				//Next are the actual arguments
				Type[] ctParams = new Type[mirror.getParameterCount() + 2];
				ctParams[0] = Type.getType(IFlower.class);
				ctParams[1] = simulatedSuperFunctionType;
				
				for(int i = 0; i < mirror.getParameterCount(); i++) {
					Type mcParam = Type.getType(mirror.getParameters()[i].getType());
					
					//Load the argument onto the stack using the appropriate loading opcode
					body.add(new VarInsnNode(mcParam.getOpcode(ILOAD), i + 1)); //+1 to skip "this" argument
					
					//Generate instructions to lift it into the CraftTweaker world
					//This method also returns the CT-lifted type so i can use it in the following method signature
					ctParams[i + 2] = TypeLifter.genCrafttweakizingCode(mcParam, body);
				}
				
				//Once they're all on the stack we can finally invoke the zenscript callable
				body.add(new MethodInsnNode(
					INVOKEINTERFACE,
					Type.getInternalName(templateField.getType()),
					templateField.getName(),
					Type.getMethodDescriptor(
						templateFunctionReturnType,
						ctParams
					),
					true
				));
				//And return!
				body.add(new InsnNode(templateFunctionReturnType.getOpcode(IRETURN)));
				
				//Finally add the method
				genMirrorOverride.instructions.add(body);
				gen.methods.add(genMirrorOverride);
			}
		} catch(Exception e) {
			throw new RuntimeException("Error generating a flower mirror method: ", e);
		}
		
		//...
		
		//Store the template in GeneratedClassSupport to be retrieved by the constructor
		GeneratedClassSupport.storeTemplate(name, flowerTemplate);
		
		//Generate and load the class
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		gen.accept(writer);
		byte[] classBytes = writer.toByteArray();
		
		//Call defineClass on the minecraft classloader.
		//It's a JDK-internal method and you have to reflect it, which is how you know it's lots of fun.
		try {
			ClassLoader launch = Launch.classLoader;
			Method defineClass = ReflectionHelper.findMethod(ClassLoader.class, "defineClass", null, String.class, byte[].class, int.class, int.class);
			//noinspection unchecked
			return (Class<? extends SubTileEntity>) defineClass.invoke(launch, gen.name.replace('/', '.'), classBytes, 0, classBytes.length);
		} catch(Exception e) {
			throw new RuntimeException("Problem defining flower class for " + name, e);
		}
	}
}
