package quaternary.zenflora.generation;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import quaternary.zenflora.IFlower;
import quaternary.zenflora.ZenFlora;
import quaternary.zenflora.annotation.Extends;
import quaternary.zenflora.annotation.Mirrors;
import quaternary.zenflora.generated.GeneratedClassSupport;
import quaternary.zenflora.templates.GenericFlowerTemplate;
import vazkii.botania.api.subtile.SubTileEntity;

import java.io.PrintWriter;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassGenerator implements Opcodes {
	private static final String TEMPLATE_FIELD_NAME = "template";
	private static final String MINI_FIELD_NAME = "isMini";
	
	public ClassGenerator(GenericFlowerTemplate flowerTemplate, String name, boolean isMini, String stacktraceSourceFile) {
		this.flowerTemplate = flowerTemplate;
		this.flowerTemplateClass = flowerTemplate.getClass();
		this.name = name;
		this.isMini = isMini;
		this.stacktraceSourceFile = stacktraceSourceFile;
	}
	
	private static final Type IFLOWER = Type.getType(IFlower.class);
	private static final Type GENERATEDCLASSSUPPORT = Type.getType(GeneratedClassSupport.class);
	private static final Handle METAFACTORY_HANDLE = new Handle(
		H_INVOKESTATIC,
		Type.getInternalName(LambdaMetafactory.class),
		"metafactory",
		Type.getMethodDescriptor(
			Type.getType(CallSite.class),
			Type.getType(MethodHandles.Lookup.class), //caller (supplied)
			Type.getType(String.class),               //invokedName (supplied)
			Type.getType(MethodType.class),           //invokedType (supplied)
			Type.getType(MethodType.class),           //samMethodType
			Type.getType(MethodHandle.class),         //implMethod
			Type.getType(MethodType.class)            //instantiatedMethodType
		),
		false
	);
	
	private final GenericFlowerTemplate flowerTemplate;
	private final Class<? extends GenericFlowerTemplate> flowerTemplateClass;
	private final String name;
	private final boolean isMini;
	private final String stacktraceSourceFile;
	
	private final ClassNode gen = new ClassNode(ASM5);
	
	private Type generatedClassType;
	private Type generatedSuperclassType;
	private Type templateClassType;
	
	public Class<? extends SubTileEntity> doIt() {
		generatedClassType = Type.getObjectType("quaternary/zenflora/generated/" + name);
		generatedSuperclassType = Type.getType(flowerTemplateClass.getAnnotation(Extends.class).value());
		
		gen.name = generatedClassType.getInternalName();
		gen.superName = generatedSuperclassType.getInternalName();
		gen.version = V1_8; //Java 8
		gen.access = ACC_PUBLIC | ACC_SYNTHETIC | ACC_SUPER;
		gen.sourceFile = stacktraceSourceFile;
		gen.interfaces.add(IFLOWER.getInternalName());
		
		templateClassType = Type.getType(flowerTemplateClass);
		
		createFields();
		createConstructor();
		
		try {
			Class<?> targetExtensionClass = flowerTemplateClass.getAnnotation(Extends.class).value();
			
			//For each field in the template class (which represents a method to override)...
			for(Field flowerTemplateField : flowerTemplateClass.getFields()) {
				//If it's null, skip it, don't generate an override for this method, don't need it
				if(flowerTemplateField.get(flowerTemplate) == null) continue;
				
				//Read the annotation
				Mirrors mirrorAnnotation = flowerTemplateField.getAnnotation(Mirrors.class);
				if(mirrorAnnotation == null) continue;
				String superMethodName = mirrorAnnotation.value();
				if(superMethodName.isEmpty()) superMethodName = flowerTemplateField.getName();
				
				//Discover the method in the target superclass that it's supposed to override
				Method superMethod = null;
				for(Method method : targetExtensionClass.getMethods()) {
					if(method.getName().equals(superMethodName)) {
						superMethod = method;
						break;
					}
				}
				if(superMethod == null) continue;
				
				//And perform the overriding
				createOverriddenMethod(flowerTemplateField, superMethod);
			}
		} catch(ReflectiveOperationException e) {
			throw new RuntimeException("Problem reflecting a flower template idk " + name, e);
		}
		
		GeneratedClassSupport.storeTemplate(name, flowerTemplate);
		
		return writeAndLoadClass();
	}
	
	public void createFields() {
		gen.visitField(
			ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
			TEMPLATE_FIELD_NAME,
			templateClassType.getDescriptor(),
			null, null
		);
		
		gen.visitField(
			ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
			MINI_FIELD_NAME,
			Type.BOOLEAN_TYPE.getDescriptor(),
			null, isMini
		);
	}
	
	public void createConstructor() {
		MethodVisitor constructor = gen.visitMethod(
			ACC_PUBLIC,
			"<init>",
			"()V",
			null, null
		);
		
		constructor.visitCode();
		
		//Call super
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(
			INVOKESPECIAL,
			generatedSuperclassType.getInternalName(),
			"<init>",
			"()V", //it's a tile entity so it always has a no-arg constructor
			false
		);
		
		//Fill in the template field
		constructor.visitLdcInsn(name);
		constructor.visitMethodInsn(
			INVOKESTATIC,
			GENERATEDCLASSSUPPORT.getInternalName(),
			"retrieveTemplate",
			Type.getMethodDescriptor(
				templateClassType,
				Type.getType(String.class)
			),
			false
		);
		constructor.visitFieldInsn(
			PUTSTATIC,
			generatedClassType.getInternalName(),
			TEMPLATE_FIELD_NAME,
			templateClassType.getDescriptor()
		);
		
		//Log to the console :)
		constructor.visitFieldInsn(
			GETSTATIC,
			Type.getInternalName(ZenFlora.class),
			"LOGGER",
			Type.getDescriptor(Logger.class)
		);
		constructor.visitLdcInsn("Hello from " + name + "'s generated constructor :)");
		constructor.visitMethodInsn(
			INVOKEINTERFACE,
			Type.getInternalName(Logger.class),
			"info",
			"(Ljava/lang/String;)V",
			true
		);
		
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(-1, -1); //compute_maxs is on.
		constructor.visitEnd();
	}
	
	public void createOverriddenMethod(Field templateField, Method superMethod) {
		//Grab some metadata
		Type superType = Type.getType(superMethod);
		Type templateFieldType = Type.getType(templateField.getType());
		
		//It's a functional interface so I can get away with this
		Method zsMethod = templateField.getType().getDeclaredMethods()[0];
		Type zsMethodType = Type.getType(zsMethod);
		//The second parameter is always the simulated return type, so I can get away with this too
		Method simSuperMethod = zsMethod.getParameterTypes()[1].getMethods()[0];
		Type simSuperType = zsMethodType.getArgumentTypes()[1];
		Type simSuperMethodType = Type.getType(simSuperMethod);
		
		//The lambda implementing the simulated call to super(...).
		String simSuperName = templateField.getName() + "$lambda";
		MethodVisitor simSuper = gen.visitMethod(
			ACC_PRIVATE,
			simSuperName,
			simSuperMethodType.getDescriptor(),
			null, null
		);
		simSuper.visitCode();
		
		simSuper.visitVarInsn(ALOAD, 0); //for invokespecial
		//Rest of supersim code filled in below
		
		//The method that overrides something in SubTileEntity/descendants.
		MethodVisitor override = gen.visitMethod(
			superMethod.getModifiers(),
			superMethod.getName(),
			superType.getDescriptor(),
			null, null //Here, banking on nothing needing a signature or a throws clause.
		);
		override.visitCode();
		
		//In the override, first, grab the ZenScript callable.
		override.visitFieldInsn(
			GETSTATIC,
			generatedClassType.getInternalName(),
			TEMPLATE_FIELD_NAME,
			templateClassType.getDescriptor()
		);
		override.visitFieldInsn(
			GETFIELD,
			templateClassType.getInternalName(),
			templateField.getName(),
			templateFieldType.getDescriptor()
		);
		
		//Start filling out the ZenScript callable's method arguments one by one.
		//First: IFlower, which is me!
		override.visitVarInsn(ALOAD, 0);
		//Actually do it 2 times not really sure why!
		override.visitVarInsn(ALOAD, 0);
		
		//Next: simulated super method, which is resolved using INVOKEDYNAMIC. The source of it appears below.
		override.visitInvokeDynamicInsn(
			superMethod.getName(),
			Type.getMethodDescriptor(
				simSuperType,
				generatedClassType
			),
			//LambdaMetafactory bootstrap method
			METAFACTORY_HANDLE,
			//samMethodType
			simSuperMethodType,
			//implMethod
			new Handle(
				H_INVOKEVIRTUAL,
				generatedClassType.getInternalName(),
				simSuperName,
				simSuperMethodType.getDescriptor(),
				false
			),
			//instantiatedMethodType
			simSuperMethodType
		);
		
		//Then: each of the "regular" arguments, in turn
		for(int i = 0; i < superMethod.getParameterCount(); i++) {
			Type mcParamType = Type.getType(superMethod.getParameters()[i].getType());
			//Load the method parameter onto the stack
			override.visitVarInsn(mcParamType.getOpcode(ILOAD), i + 1);
			//Generate code needed to lift into the Crafttweaker world
			//(and hang on to the CraftTweakerized type)
			Type ctLiftedType = TypeLifter.genLifting(mcParamType, override);
			
			//While we're busy iterating over types, fill out the super sim
			//Same deal but in the lambda this time, load them onto the stack
			simSuper.visitVarInsn(ctLiftedType.getOpcode(ILOAD), i + 1);
			//This time drop them back into the Minecraft world
			TypeLifter.genUnlifting(mcParamType /* mc is not a typo */, simSuper);
		}
		
		//Finally in the method: call the zenscript functional interface
		override.visitMethodInsn(
			INVOKEINTERFACE,
			templateFieldType.getInternalName(),
			templateField.getName(),
			zsMethodType.getDescriptor(),
			true
		);
		
		//And return whatever type it returned
		override.visitInsn(superType.getReturnType().getOpcode(IRETURN));
		
		//Turning attention back to the lambda function...
		//Actually call the super method now, everything should be set up.
		simSuper.visitMethodInsn(
			INVOKESPECIAL,
			generatedSuperclassType.getInternalName(),
			superMethod.getName(),
			superType.getDescriptor(),
			false
		);
		
		//Lift the return type back into the ZenScript world
		//TODO: info shoving (reflecting CTMC making copies for objects like IData)
		if(!simSuperMethodType.getReturnType().equals(Type.VOID_TYPE)) {
			Type ctLiftedType = TypeLifter.genLifting(simSuperMethodType.getReturnType(), simSuper);
			simSuper.visitInsn(ctLiftedType.getOpcode(IRETURN));
		} else {
			simSuper.visitInsn(RETURN);
		}
		
		//Tidy up
		override.visitMaxs(-1, -1);
		override.visitEnd();
		simSuper.visitMaxs(-1, -1);
		simSuper.visitEnd();
	}
	
	public Class<? extends SubTileEntity> writeAndLoadClass() {
		//Generate and load the class
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		gen.accept(writer);
		byte[] classBytes = writer.toByteArray();
		
		//Test it TODO remove this, it's slow and spammy ;)
		ClassReader reader = new ClassReader(classBytes);
		try {
			CheckClassAdapter.verify(reader, Launch.classLoader, true, new PrintWriter(System.err));
		} catch(Exception e) {
			ZenFlora.LOGGER.error("CheckClassAdapter failure: ", e);
		}
		
		//Call defineClass on the minecraft classloader.
		//It's a JDK-internal method and you have to reflect it, which is how you know it's lots of fun.
		try {
			ClassLoader launch = Launch.classLoader;
			Method defineClass = ReflectionHelper.findMethod(ClassLoader.class, "defineClass", null, String.class, byte[].class, int.class, int.class);
			//noinspection unchecked
			return (Class<? extends SubTileEntity>) defineClass.invoke(launch, generatedClassType.getClassName(), classBytes, 0, classBytes.length);
		} catch(Exception e) {
			throw new RuntimeException("Problem defining flower class for " + name, e);
		}
	}
}
