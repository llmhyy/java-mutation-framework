package jmutation.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;


@SuppressWarnings("restriction")
public class JavaUtil {
	private static final String TO_STRING_SIGN= "()Ljava/lang/String;";
	private static final String TO_STRING_NAME= "toString";

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static CompilationUnit parseCompilationUnit(String file){
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(false);
		
		try {
			String text = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
			
			parser.setSource(text.toCharArray());
			
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			return cu;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static String retrieveStringValueOfArray(ArrayReference arrayValue) {
		String stringValue;
		List<Value> list = new ArrayList<>();
		if(arrayValue.length() > 0){
			list = arrayValue.getValues(0, arrayValue.length()); 
		}
		StringBuffer buffer = new StringBuffer();
		for(Value v: list){
			String valueString = (v != null) ? v.toString() : "\"null\"";
			buffer.append(valueString);
			buffer.append(",");
		}
		stringValue = buffer.toString();
		return stringValue;
	}
	
	public synchronized static String toPrimitiveValue(ClassType type, ObjectReference value,
			ThreadReference thread) {
		Method method = type.concreteMethodByName(TO_STRING_NAME, TO_STRING_SIGN);
		if (method != null) {
			try {
				if (thread.isSuspended()) {
					if (value instanceof StringReference) {
						return ((StringReference) value).value();
					}
					Field field = type.fieldByName("value");
					Value toStringValue = value.getValue(field);
//					Value toStringValue = value.invokeMethod(thread, method,
//							new ArrayList<Value>(),
//							ObjectReference.INVOKE_SINGLE_THREADED);
					return toStringValue.toString();
					
				}
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	/**
	 * generate signature such as methodName(java.lang.String)L
	 * @param md
	 * @return
	 */
	public static String generateMethodSignature(IMethodBinding mBinding){
//		IMethodBinding mBinding = md.resolveBinding();
		
		String returnType = mBinding.getReturnType().getKey();
		
		String methodName = mBinding.getName();
		
		List<String> paramTypes = new ArrayList<>();
		for(ITypeBinding tBinding: mBinding.getParameterTypes()){
			String paramType = tBinding.getKey();
			paramTypes.add(paramType);
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(methodName);
		buffer.append("(");
		for(String pType: paramTypes){
			buffer.append(pType);
			//buffer.append(";");
		}
		
		buffer.append(")");
		buffer.append(returnType);
//		
//		String sign = buffer.toString();
//		if(sign.contains(";")){
//			sign = sign.substring(0, sign.lastIndexOf(";")-1);			
//		}
//		sign = sign + ")" + returnType;
		
		String sign = buffer.toString();
		
		return sign;
	}
	
	public static String getFullNameOfCompilationUnit(CompilationUnit cu){
		
		String packageName = "";
		if(cu.getPackage() != null){
			packageName = cu.getPackage().getName().toString();
		}
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) cu.types().get(0);
		String typeName = typeDeclaration.getName().getIdentifier();
		
		if(packageName.length() == 0){
			return typeName;
		}
		else{
			return packageName + "." + typeName; 			
		}
		
	}

	public static IProject getSpecificJavaProjectInWorkspace(String projectName){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		
		for(int i=0; i<projects.length; i++){
			if(projects[i].getName().equals(projectName)){
				return projects[i];
			}
		}
		return null;
	}

	public static HashMap<String, CompilationUnit> sourceFile2CUMap = new HashMap<>();
	
	public static CompilationUnit findCompiltionUnitBySourcePath(String javaFilePath, 
			String declaringCompilationUnitName) {
		
		CompilationUnit parsedCU = sourceFile2CUMap.get(javaFilePath);
		if(parsedCU != null) {
			return parsedCU;
		}
		
		File javaFile = new File(javaFilePath);
		
		if(javaFile.exists()){
			
			String contents;
			try {
				contents = new String(Files.readAllBytes(Paths.get(javaFilePath)));
				
				final ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setSource(contents.toCharArray());
				parser.setResolveBindings(true);
				
				CompilationUnit cu = (CompilationUnit)parser.createAST(null);
				sourceFile2CUMap.put(javaFilePath, cu);
				
				return cu;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			System.err.print("cannot find " + declaringCompilationUnitName + " under " + javaFilePath);			
		}
		
		return null;
	}
}
