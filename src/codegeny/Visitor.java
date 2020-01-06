package codegeny;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import model.FieldType;

public class Visitor extends ASTVisitor {
	
	public List<FieldType> fieldTypeObj = new ArrayList<FieldType>();
	public List<String> methodNames = new ArrayList<String>();
	public String className = "";
	
	@Override
	public boolean visit(FieldDeclaration node) {
		// loop for several variables in the same declaration
		for(Object o : node.fragments()) {
			VariableDeclarationFragment var = (VariableDeclarationFragment) o;
			String name = var.getName().toString();
			String type = node.getType().toString();
			boolean isStatic  = Modifier.isStatic(node.getModifiers());
			
			if(!isStatic) {
				fieldTypeObj.add(new FieldType(name, type));
			}
			
		}
		return false;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		methodNames.add(node.getName().getIdentifier());
		return super.visit(node);
	}
	
	@Override
	public boolean visit(CompilationUnit node) {
		TypeDeclaration type = (TypeDeclaration) node.types().get(0);
		
		if(type != null)
			className = type.getName().toString();
		
		return super.visit(node);
	}
}
