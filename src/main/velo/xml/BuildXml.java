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
package velo.xml;

import org.apache.ecs.xml.XML;
import org.apache.ecs.xml.XMLDocument;

public class BuildXml {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XMLDocument document = new XMLDocument();
		document.setCodeset("UTF-8");
		XML ifRequest = new XML("IF_REQUEST");
		//Add the ifRequest element to the document
		document.addElement(ifRequest);

		ifRequest.addAttribute("IF_SERVICE_NAME", "EMPLOYEE_INFO");
		XML serviceName = new XML("SERVICE_NAME");
		serviceName.setTagText("EMPLOYEE_INFO");
		//Add the serviceName element to the document
		document.addElement(serviceName);
		
		XML params = new XML("PARAMS_LIST");
		//Attrs
		XML employeeIntegrationId = new XML("Employee_IntegrationId");
		employeeIntegrationId.setTagText("233932");
		params.addElement(employeeIntegrationId);
		XML employeeBuildingNumber = new XML("Employee_BuildingNumber");
		employeeBuildingNumber.setTagText("rosh haayin");
		params.addElement(employeeBuildingNumber);
		XML employeeCellPhone = new XML("Employee_CellPhone");
		employeeCellPhone.setTagText("+9720542230794");
		params.addElement(employeeCellPhone);
		XML employeeNumber = new XML("Employee_Emp");
		employeeNumber.setTagText("038259305");
		params.addElement(employeeNumber);
		XML employeeTypeCode = new XML("Employee_EmployeeTypeCode");
		employeeTypeCode.setTagText("---KABLAN---");
		params.addElement(employeeTypeCode);
		XML employeeStatus = new XML("Employee_EmploymentStatus");
		employeeStatus.setTagText("Active Assignment");
		params.addElement(employeeStatus);
		XML firstName = new XML("Employee_FirstName");
		firstName.setTagText("--ORIT--");
		params.addElement(firstName);
		XML lastName = new XML("Employee_LastName");
		lastName.setTagText("--ETZYONI--");
		params.addElement(lastName);
		XML hireDate = new XML("Employee_HireDate");
		hireDate.setTagText("12/03/2006");
		params.addElement(hireDate);
		XML jobTitle = new XML("Employee_JobTitle");
		jobTitle.setTagText("--OVED KABLAN--");
		params.addElement(jobTitle);
		XML employeeLoginName = new XML("Employee_LoginName");
		employeeLoginName.setTagText("OEZIONI");
		params.addElement(employeeLoginName);
		XML birthDate = new XML("Employee_PTSBirthDate");
		birthDate.setTagText("11/24/1975");
		params.addElement(birthDate);
		XML employeeSSN = new XML("Employee_PTSEmployeeSSN");
		employeeSSN.setTagText("038259305");
		params.addElement(employeeSSN);
		
		//Add the parameters element to the document
		document.addElement(params);
		
		System.out.println(document.toString());
	}

}
