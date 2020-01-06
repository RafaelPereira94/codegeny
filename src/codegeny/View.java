package codegeny;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
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
	
	private static final String RETURN_TRUE = "return true;";
	private static final String RETURN_FALSE = "return false;";
	private static final String EQUALS = "=";
	private static final String NEW_LINE = "\n";
	private static final String TAB = "\t";
	private static final String OVERRIDE = "@Override";
	private static final String SPACE = " ";
	private static final String EXTENSIONPOINT_NAME = "codegeny.ExtensionPointGenerator";

	public View() {}

	@Override
	public void createContents(Composite viewArea, Map<String, Image> imageMap) {
		viewArea.setLayout(new RowLayout(SWT.VERTICAL));
		Label label = new Label(viewArea, SWT.NONE);
		label.setText("Code Generator options:");
		
		BundleContext context =  Activator.getContext();
		ServiceReference<JavaEditorServices> serviceReference = context.getServiceReference(JavaEditorServices.class);
		JavaEditorServices javaEditorServices = context.getService(serviceReference);
		
		File openFileEditor = javaEditorServices.getOpenedFile();
		
		Button hashcodeBtn = new Button(viewArea, SWT.PUSH);
		hashcodeBtn.setText("Generate HashCode()");
		
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		for (IExtension ext : reg.getExtensionPoint(EXTENSIONPOINT_NAME).getExtensions()) {
			Button generateExtencionCode = new Button(viewArea, SWT.NONE);
			generateExtencionCode.setText("Generate Ext" + ext.getExtensionPointUniqueIdentifier().length());
			generateExtencionCode.addListener(SWT.Selection, event -> {
				IGenerator extension = null;

				for (IConfigurationElement iconf : ext.getConfigurationElements()) {

					try {
						extension = (IGenerator) iconf.createExecutableExtension("class");
					} catch (InvalidRegistryObjectException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

				String codGen = extension.generate();
				javaEditorServices.insertTextAtCursor(codGen);
			});
		}
		
		hashcodeBtn.addListener(SWT.Selection, event -> {
			switch (event.type) {
			case SWT.Selection:
				
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
				
				Visitor visitor = new Visitor();
				JavaParser.parse(openFileEditor, visitor);
				List<String> methodList = visitor.methodNames;
				List<FieldType> fieldTypeList = visitor.fieldTypeObj;
				String className = visitor.className;
				
				//if the method already exists in the class we don't show up.
				
				if(!methodList.contains("toString")) {
					String method = writeToStringMethod(fieldTypeList, className, true);
					javaEditorServices.insertTextAtCursor(method);
				}
				
			}
		});
	}

	private String writeHashCode(List<FieldType> fieldTypeList) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(OVERRIDE).append(NEW_LINE);
		sb.append(TAB).append("public int hashCode() {").append(NEW_LINE);
		sb.append(TAB).append(TAB).append("final int prime = 31;").append(NEW_LINE);
		sb.append(TAB).append(TAB).append("int result = 1;").append(NEW_LINE);
		
		for(FieldType field : fieldTypeList) {
			
			if(field.getType().equals("int") || field.getType().equals("double") || field.getType().equals("float") 
					|| field.getType().equals("short") || field.getType().equals("byte") || field.getType().equals("long")
					|| field.getType().equals("char")) { //we just sum the year with result * prime
				sb.append(TAB).append(TAB).append("result = prime * result + ").append(field.getName()).append(";").append(NEW_LINE);
			}else if(field.getType().equals("boolean")) {
				sb.append(TAB).append(TAB).append("result = prime * result + ").append("Boolean.hashCode(").append(field.getName()).append(");").append(NEW_LINE);
			}
			else { //we sum the HashCode of field
				sb.append(TAB).append(TAB).append("result = prime * result + ").append("((").append(field.getName()).append(" == null) ? 0 : ").append(field.getName()).append(".hashCode());\n");
			}
		}
		
		sb.append(TAB).append(TAB).append("return result;").append(NEW_LINE);
		sb.append(TAB).append("}");
		return sb.toString();
	}

	
	private String writeToStringMethod(List<FieldType> fieldTypeList, String className, boolean skipNulls) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("@Override").append(NEW_LINE);
		sb.append(TAB).append("public String toString() {").append(NEW_LINE);
		sb.append(TAB).append(SPACE).append("return").append(" \"").append(className).append(SPACE).append("[");
		
		for(int i = 0; i < fieldTypeList.size(); i++) {
			FieldType type = fieldTypeList.get(i);
			
			sb.append(type.getName()).append(EQUALS).append("\" + ").append(type.getName());
		
			if(i < fieldTypeList.size() -1) {
				sb.append(" + \", ");
			}else {
				sb.append(" + \"]\"");
				sb.append(";").append(NEW_LINE);
			}
		}
		
		sb.append(TAB).append("}").append(NEW_LINE);
		return sb.toString();
	}
	
	
	private String writeEqualsMethod(List<FieldType> fieldTypeList, String className) {
		StringBuilder sb = new StringBuilder();
		sb.append(OVERRIDE).append(NEW_LINE);
		sb.append(TAB).append("public boolean equals(Object obj) {").append(NEW_LINE);
		sb.append(TAB).append(TAB).append("if (this == obj)").append(NEW_LINE);
		sb.append(TAB).append(TAB).append(TAB).append(RETURN_TRUE).append(NEW_LINE);
		sb.append(TAB).append(TAB).append("if (obj == null)").append(NEW_LINE);
		sb.append(TAB).append(TAB).append(TAB).append(RETURN_FALSE).append(NEW_LINE);
		sb.append(TAB).append(TAB).append("if( getClass() != obj.getClass())").append(NEW_LINE);
		sb.append(TAB).append(TAB).append(TAB).append(RETURN_FALSE).append(NEW_LINE);
		sb.append(TAB).append(TAB).append(className).append(" ").append("other = ").append("(").append(className).append(") ").append("obj;").append(NEW_LINE);
		
		for(FieldType field : fieldTypeList) {
			
			if(isPrimitiveType(field)) {
				sb.append(TAB).append(TAB).append("if(").append(field.getName()).append(" != ").append("other").append(".").append(field.getName()).append(")").append(NEW_LINE);
				sb.append(TAB).append(TAB).append(TAB).append(RETURN_FALSE).append(NEW_LINE);
			}else {
				sb.append(TAB).append(TAB).append("if (").append(field.getName()).append(" == null )").append("{").append(NEW_LINE);
				
				sb.append(TAB).append(TAB).append(TAB).append("if (").append("other").append(".").append(field.getName()).append(" != null)").append(NEW_LINE);
				sb.append(TAB).append(TAB).append(TAB).append(RETURN_FALSE).append(NEW_LINE);
				
				sb.append(TAB).append(TAB).append("} else if (!").append(field.getName()).append(".equals(other.").append(field.getName()).append("))").append(NEW_LINE);
				sb.append(TAB).append(TAB).append(TAB).append(RETURN_FALSE).append(NEW_LINE);
			}
		}
		
		sb.append(TAB).append(TAB).append(RETURN_TRUE).append(NEW_LINE);
		sb.append(TAB).append("}").append(NEW_LINE);
		return sb.toString();
	}

	private boolean isPrimitiveType(FieldType field) {
		return field.getType().equals("int") || field.getType().equals("double") || field.getType().equals("boolean") || field.getType().equals("float") 
				|| field.getType().equals("short") || field.getType().equals("byte") || field.getType().equals("long")
				|| field.getType().equals("char");
	}
}
