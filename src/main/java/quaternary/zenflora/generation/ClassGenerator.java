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
import quaternary.zenflora.ZenFlora;
import quaternary.zenflora.annotation.Extends;
import quaternary.zenflora.annotation.Mirrors;
import quaternary.zenflora.generated.GeneratedClassSupport;
import quaternary.zenflora.templates.GenericFlowerTemplate;
import vazkii.botania.api.subtile.SubTileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassGenerator implements Opcodes {
	public static Class<? extends SubTileEntity> doIt(GenericFlowerTemplate flowerTemplate, String name, boolean mini) {
		ClassNode gen = new ClassNode(Opcodes.ASM5);
		Class<?> templateClass = flowerTemplate.getClass();
		Class<?> targetSuperclass = templateClass.getAnnotation(Extends.class).value();
		
		gen.name = "quaternary/zenflora/generated/" + name;
		gen.superName = Type.getInternalName(targetSuperclass);
		gen.version = V1_8; //Java 8
		gen.access = ACC_PUBLIC | ACC_SYNTHETIC;
		
		//Generate a field to store the flower template in
		String templateFieldName = "template";
		FieldNode templateField = new FieldNode(
			ASM5,
			ACC_PRIVATE | ACC_FINAL,
			templateFieldName,
			Type.getDescriptor(templateClass),
			null, null
		);
		gen.fields.add(templateField);
		
		//And a field to store the mini-ness of the flower in.
		//Pass in the mininess now since I already know it.
		String miniFieldName = "mini";
		FieldNode miniField = new FieldNode(
			ASM5,
			ACC_PRIVATE | ACC_FINAL,
			miniFieldName,
			Type.BOOLEAN_TYPE.getDescriptor(),
			null,
			mini
		);
		gen.fields.add(miniField);
		
		//Generate a no-arg public constructor.
		//This fills in the "template" field by hitting up GeneratedClassSupport.
		MethodNode constructor = new MethodNode(ASM5, ACC_PUBLIC, "<init>", "()V", null, null);
		InsnList consInsns = new InsnList();
		consInsns.add(new VarInsnNode(ALOAD, 0)); //this
		consInsns.add(new LdcInsnNode(name)); //template name
		consInsns.add(new MethodInsnNode(
			INVOKESTATIC,
			Type.getInternalName(GeneratedClassSupport.class),
			"retrieveTemplate",
			Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),
			false
		));
		consInsns.add(new FieldInsnNode(
			PUTSTATIC,
			gen.name,
			templateFieldName,
			Type.getDescriptor(templateClass)
		));
		
		//Also, while we're here, say hi to the console!
		consInsns.add(new LdcInsnNode("Hello from " + name + "'s generated constructor!"));
		consInsns.add(new FieldInsnNode(
			GETSTATIC,
			Type.getInternalName(ZenFlora.class),
			"LOGGER",
			Type.getDescriptor(Logger.class)
		));
		consInsns.add(new MethodInsnNode(
			INVOKEINTERFACE,
			Type.getInternalName(Logger.class),
			"info",
			Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),
			true
		));
		
		consInsns.add(new InsnNode(RETURN));
		
		constructor.instructions.add(consInsns);
		gen.methods.add(constructor);
		
		//For each non-null field in the template...
		try {
			for(Field field : flowerTemplate.getClass().getFields()) { //getFields respects inheritance
				if(field.get(flowerTemplate) == null) continue;
				
				//find out what method it's supposed to implement in the target superclass
				Mirrors mirrorAnnotation = field.getAnnotation(Mirrors.class);
				if(mirrorAnnotation == null) continue;
				
				String mirrorName = mirrorAnnotation.value();
				if(mirrorName.isEmpty()) mirrorName = field.getName(); //"" is the default in the annotation
				
				Method mirror = null;
				for(Method mirrorCandidate : targetSuperclass.getMethods()) { //also respects inheritance
					if(mirrorCandidate.getName().equals(mirrorName)) {
						mirror = mirrorCandidate;
						break;
					}
				}
				
				if(mirror == null) throw new IllegalStateException("Can't find mirror for template field " + field.getName());
				
				//Generate an override for this method
				MethodNode mirrorOverride = new MethodNode(
					ASM5,
					ACC_PUBLIC,
					mirror.getName(),
					Type.getMethodDescriptor(mirror),
					null, //Banking on nothing needing a signature
					null  //Banking on nothing needing a throws clause
				);
				
				//Generate the method body of the override
				//...
				
				//Add the method
				gen.methods.add(mirrorOverride);
				
			}
		} catch(IllegalAccessException | IllegalStateException e) {
			throw new RuntimeException("Error reflecting the flower template: ", e);
		}
		
		//...
		
		//Store the template in GeneratedClassSupport to be retrieved later
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
