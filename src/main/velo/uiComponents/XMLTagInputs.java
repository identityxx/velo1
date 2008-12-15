/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.uiComponents;

import java.io.IOException;
import java.util.Iterator;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class XMLTagInputs extends UIComponentBase {

	private String tagName;
	private String forVal;
	private boolean renderSubNodes = true;

	public XMLTagInputs() {

		// Can't perform the rendering in here, since the component attributes
		// are not set yet here.
	}

	public void encodeBegin(FacesContext context) throws IOException {
		if (getChildren().size() == 0) {
			// Get the view root from the Context
			UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
			// System.out.println("WAAAAAAAAAAAAAAAAAAA VIEW: " + view);
			//System.out.println("GET FOR: " + getFor());
			// Get the XMLManager component from the view
			UIComponent uiComponent = view.findComponent(getFor());

			// System.out.println("Loaded component that expected to be an
			// XMLManager of class \'" + uiComponent.getClass().getName() +
			// "\'");
			if (uiComponent instanceof XMLManager) {
				XMLManager xmlManager = (XMLManager) uiComponent;
				// System.out.println("xml conf: " + xmlManager.getConfig());
				try {
					HierarchicalConfiguration hc = xmlManager.getConfig()
							.getConfig().configurationAt(getTagName());

					Iterator nodes = hc.getRootNode().getChildren()
							.listIterator();

					HtmlPanelGrid hpg = (HtmlPanelGrid) this.getFacesContext()
							.getApplication().createComponent(
									HtmlPanelGrid.COMPONENT_TYPE);
					hpg.setColumns(1);
					hpg.setStyleClass("xmlTagMainPanel");

					// System.out.println("Size of children(BEFORE): " +
					// getChildren().size());

					while (nodes.hasNext()) {
						XMLConfiguration.Node currNode = (XMLConfiguration.Node) nodes
								.next();

						// If node has childs, then iterate over childs and
						// display them
						if (currNode.getChildrenCount() > 0) {
							if (renderSubNodes) {
								Iterator subNodes = currNode.getChildren()
										.listIterator();
								while (subNodes.hasNext()) {
									XMLConfiguration.Node currSubNode = (XMLConfiguration.Node) subNodes
											.next();
									XMLTagInput xti = new XMLTagInput(
											XMLTagInput
													.getFullXmlTagPath(currSubNode),
											getFor());
									// hpg.getChildren().add(getTag(currSubNode));
									hpg.getChildren().add(xti);
								}
							} else {
								// Specified not to render tags with childs,
								// then continue to the next node
								continue;
							}
							// Else render tag since this is not a parent
						} else {
							// hpg.getChildren().add(getTag(currNode));
							XMLTagInput xti = new XMLTagInput(XMLTagInput
									.getFullXmlTagPath(currNode), getFor());
							hpg.getChildren().add(xti);
						}

						this.getChildren().add(hpg);

					}

				} catch (ConfigurationException ce) {
					ce.printStackTrace();
				}

			} else {
				// TODO handle the exception correctly!
				//System.out.println("Expected UI Component is not an instance of XMLManager class!");
			}

			// System.out.println("Size of children(AFTER): " +
			// getChildren().size());
		} else {
			// this component already has childrens, probably from the restore
			// phase, then do nothing...
		}
	}

	// // public void encodeBegin(FacesContext context) throws IOException {
	// /*
	// System.out.println("Encoding XMLTagInput component with tag named: " +
	// tagName);
	// System.out.println("Component is relevant to XMLManager ID: " +
	// getFor());
	// //UiXMLManager is not not always the parent
	// //XMLManager xmlManager = (XMLManager)getParent();
	// UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
	// UIComponent uiComponent = view.findComponent(getFor());
	//
	// System.out.println("Loaded component that expected to be an XMLManager of
	// class \'" + uiComponent.getClass().getName() + "\'");
	//
	// if (uiComponent instanceof XMLManager) {
	// XMLManager xmlManager = (XMLManager) uiComponent;
	// //System.out.println("xml conf: " + xmlManager.getConfig());
	// HierarchicalConfiguration hc =
	// xmlManager.getConfig().getConfig().configurationAt(getTagName());
	// Iterator nodes = hc.getRootNode().getChildren().listIterator();
	//
	// while (nodes.hasNext()) {
	// HtmlInputText inputTag = (HtmlInputText) this.getFacesContext()
	// .getApplication().createComponent(HtmlInputText.COMPONENT_TYPE);
	// HtmlOutputText outputTag = (HtmlOutputText) this.getFacesContext()
	// .getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
	//
	//
	// XMLConfiguration.Node conf = (XMLConfiguration.Node) nodes.next();
	// inputTag.setValue(conf.getValue());
	// List attrList = conf.getAttributes();
	// Iterator attrListIt = attrList.iterator();
	// while (attrListIt.hasNext()) {
	// XMLConfiguration.Node attr = (XMLConfiguration.Node) attrListIt.next();
	// if (attr.getName().equalsIgnoreCase("description")) {
	// outputTag.setValue(attr.getValue());
	// }
	// }
	//
	// getChildren().add(outputTag);
	// getChildren().add(inputTag);
	// }
	//
	// super.encodeBegin(context);
	// } else {
	// //TODO handle the exception correctly!
	// System.out.println("Expected UI Component is not an instance of
	// XMLManager class!");
	// }
	// * */
	// // }
	//
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * <p>
	 * Return the client identifier of the component for which this component
	 * represents associated message(s) (if any).
	 * </p>
	 */
	public String getFor() {
		if (this.forVal != null) {
			return this.forVal;
		}

		ValueExpression ve = getValueExpression("for");
		if (ve != null) {
			try {
				return (String) ve.getValue(getFacesContext().getELContext());
			} catch (ELException e) {
				throw new FacesException(e);
			}
		} else {
			return null;
		}
	}

	/**
	 * <p>
	 * Set the client identifier of the component for which this component
	 * represents associated message(s) (if any). This property must be set
	 * before the message is displayed.
	 * </p>
	 * 
	 * @param newFor
	 *            The new client id
	 */
	public void setFor(String newFor) {
		forVal = newFor;
	}

	//
	//
	//
	// //STATE MANAGEMENT
	private Object[] values;

	//
	public Object saveState(FacesContext context) {
		if (values == null) {
			values = new Object[2];
		}
		//
		values[0] = super.saveState(context);
		values[1] = this.forVal;
		return values;
	}

	//
	//
	public void restoreState(FacesContext context, Object state) {
		values = (Object[]) state;
		super.restoreState(context, values[0]);
		forVal = (String) values[1];
	}

	//
	public String getFamily() {
		return "velo.xmlManager";
	}
}
