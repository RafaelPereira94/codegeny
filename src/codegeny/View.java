package codegeny;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import model.FieldType;
import pt.iscte.pidesco.extensibility.PidescoView;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;

public class View implements PidescoView {
	
	Visitor visitor = new Visitor();

	public View() {
		
	}

	@Override
	public void createContents(Composite viewArea, Map<String, Image> imageMap) {
		viewArea.setLayout(new RowLayout(SWT.VERTICAL));
		Label label = new Label(viewArea, SWT.NONE);
		label.setImage(imageMap.get("smiley.png"));

		Button hashcodeBtn = new Button(viewArea, SWT.PUSH);
		hashcodeBtn.setText("Generate HashCode()");

		hashcodeBtn.addListener(SWT.Selection, event -> {
			switch (event.type) {
			case SWT.Selection:
				BundleContext context =  Activator.getContext();
				ServiceReference<JavaEditorServices> serviceReference = context.getServiceReference(JavaEditorServices.class);
				JavaEditorServices javaEditorServices = context.getService(serviceReference);
				
				File openFileEditor = javaEditorServices.getOpenedFile();
				
				Visitor visitor = new Visitor();
				JavaParser.parse(openFileEditor, visitor);
				List<String> methodList = visitor.methodNames;
				List<FieldType> fieldTypeList = visitor.fieldTypeObj;
				
				if(!methodList.contains("hashCode")) {
					String method = writeHashCode(fieldTypeList);
					javaEditorServices.insertTextAtCursor(method);
				}
				
				
				break;
			}
		});

		Button equalsBtn = new Button(viewArea, SWT.PUSH);
		equalsBtn.setText("Generate equals()");
		
		equalsBtn.addListener(SWT.Selection, event -> {
			switch (event.type) {
			case SWT.Selection:
				BundleContext context =  Activator.getContext();
				ServiceReference<JavaEditorServices> serviceReference = context.getServiceReference(JavaEditorServices.class);
				JavaEditorServices javaEditorServices = context.getService(serviceReference);
				
				File openFileEditor = javaEditorServices.getOpenedFile();
				
				Visitor visitor = new Visitor();
				JavaParser.parse(openFileEditor, visitor);
				List<String> methodList = visitor.methodNames;
				List<FieldType> fieldTypeList = visitor.fieldTypeObj;
				String className = visitor.className;
				
				//if the method already exists in the class we don't show up.
				
				if(!methodList.contains("equals")) {
					String method = writeEqualsMethod(fieldTypeList, className);
					javaEditorServices.insertTextAtCursor(method);
				}
				
				break;
			}
		});
		
		
		Button toStringBtn = new Button(viewArea, SWT.PUSH);
		toStringBtn.setText("Generate toString()");
		toStringBtn.addListener(SWT.Selection, event -> {
			switch (event.type) {
			case SWT.Selection:
				BundleContext context =  Activator.getContext();
				ServiceReference<JavaEditorServices> serviceReference = context.getServiceReference(JavaEditorServices.class);
				JavaEditorServices javaEditorServices = context.getService(serviceReference);
				
				File openFileEditor = javaEditorServices.getOpenedFile();
				
				Visitor visitor = new Visitor();
				JavaParser.parse(openFileEditor, visitor);
				List<String> methodList = visitor.methodNames;
				List<FieldType> fieldTypeList = visitor.fieldTypeObj;
				String className = visitor.className;
				
				//if the method already exists in the class we don't show up.
				
				if(!methodList.contains("toString")) {
					String method = writeToStringMethod(fieldTypeList, className);
					javaEditorServices.insertTextAtCursor(method);
				}
				
			}
		});
	}
	
	

	private String writeHashCode(List<FieldType> fieldTypeList) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("@Override").append("\n");
		sb.append("\tpublic int hashCode() {").append("\n");
		sb.append("\t").append("\t").append("final int prime = 31;").append("\n");
		sb.append("\t").append("\t").append("int result = 1;").append("\n");
		
		for(FieldType field : fieldTypeList) {
			
			if(field.getType().equals("int") || field.getType().equals("double") || field.getType().equals("float") 
					|| field.getType().equals("short") || field.getType().equals("byte") || field.getType().equals("long")
					|| field.getType().equals("char")) { //we just sum the year with result * prime
				sb.append("\t").append("\tresult = prime * result + ").append(field.getName()).append(";").append("\n");
			}else if(field.getType().equals("boolean")) {
				sb.append("\t").append("\tresult = prime * result + ").append("Boolean.hashCode(").append(field.getName()).append(");").append("\n");
			}
			else { //we sum the HashCode of field
				sb.append("\t").append("\tresult = prime * result + ").append("((").append(field.getName()).append(" == null) ? 0 : ").append(field.getName()).append(".hashCode());\n");
			}
		}
		
		sb.append("\t").append("\t").append("return result;").append("\n");
		sb.append("\t").append("}");
		return sb.toString();
	}

	
	private String writeToStringMethod(List<FieldType> fieldTypeList, String className) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("@Override \n");
		sb.append("\tpublic String toString() {\n");
		sb.append("\t return").append(" \"").append(className).append(" ").append("[");
		for(int i = 0; i < fieldTypeList.size(); i++) {
			FieldType type = fieldTypeList.get(i);
			
			sb.append(type.getName()).append("=").append("\" + ").append(type.getName());
			
			if(i < fieldTypeList.size() -1) {
				sb.append(" + \", ");
			}else {
				sb.append(" + \"]\"");
				sb.append(";\n");
			}
		}
		
		sb.append("\t}\n");
		return sb.toString();
	}
	
	
	private String writeEqualsMethod(List<FieldType> fieldTypeList, String className) {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override").append("\n");
		sb.append("\t").append("public boolean equals(Object obj) {").append("\n");
		sb.append("\t").append("\t").append("if (this == obj)").append("\n");
		sb.append("\t").append("\t").append("\t").append("return true;").append("\n");
		sb.append("\t").append("\t").append("if (obj == null)").append("\n");
		sb.append("\t").append("\t").append("\t").append("return false;").append("\n");
		sb.append("\t").append("\t").append("if( getClass() != obj.getClass())").append("\n");
		sb.append("\t").append("\t").append("\t").append("return false;").append("\n");
		sb.append("\t").append("\t").append(className).append(" ").append("other = ").append("(").append(className).append(") ").append("obj;").append("\n");
		
		for(FieldType field : fieldTypeList) {
			
			if(field.getType().equals("int") || field.getType().equals("double") || field.getType().equals("boolean") || field.getType().equals("float") 
					|| field.getType().equals("short") || field.getType().equals("byte") || field.getType().equals("long")
					|| field.getType().equals("char")) {
				sb.append("\t").append("\t").append("if(").append(field.getName()).append(" != ").append("other").append(".").append(field.getName()).append(")").append("\n");
				sb.append("\t").append("\t").append("\t").append("return false;").append("\n");
			}else {
				sb.append("\t").append("\t").append("if (").append(field.getName()).append(" == null )").append("{").append("\n");
				
				sb.append("\t").append("\t").append("\t").append("if (").append("other").append(".").append(field.getName()).append(" != null)").append("\n");
				sb.append("\t").append("\t").append("\t").append("return false;").append("\n");
				
				sb.append("\t").append("\t").append("} else if (!").append(field.getName()).append(".equals(other.").append(field.getName()).append("))").append("\n");
				sb.append("\t").append("\t").append("\t").append("return false;").append("\n");
			}
		}
		
		sb.append("\t").append("\t").append("return true;").append("\n");
		sb.append("\t").append("}").append("\n");
		return sb.toString();
	}
}
