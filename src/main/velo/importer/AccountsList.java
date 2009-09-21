
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
package velo.importer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author Shakarchi Asaf
 */
public class AccountsList extends ArrayList<ImportAccount> {
    
    public AccountsList() {
    }
    
    
    public void importFromXls(String fileName, String spreadSheetName) throws Exception {
        FileInputStream is = new FileInputStream(fileName);
        importFromXls(is, spreadSheetName);
    }
    
    public void importFromXls(InputStream is, String spreadSheetName) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook(is);
        
        //Count the number of sheets
        if (workbook.getNumberOfSheets() < 1) {
            throw new Exception("Number of sheets in excel is less than 1!");
        }
        
        HSSFSheet sheet = workbook.getSheet(spreadSheetName);
        if (sheet == null) {
            throw new Exception("Could not find sheet named '" + spreadSheetName + "'");
        }
        
        
        //todo 3 cells at least!
        
        //Make sure the headers correspond to the expected values
        HSSFRow header = sheet.getRow(0);
        HSSFCell accountNameTitle = header.getCell((short)0);
        HSSFCell targetNameTitle = header.getCell((short)1);
        
        if (!accountNameTitle.toString().equalsIgnoreCase("ACCOUNT")) {
            throw new Exception("Column one in first row must equal to 'ACCOUNT' and represents the Account to associate");
        }
        if (!targetNameTitle.toString().equalsIgnoreCase("RESOURCE_UNIQUE_NAME")) {
            throw new Exception("Column one in first row must equal to 'TARGET-SYSTEM' and represents the resource unique name the account is related to!");
        }
        
        
        for (int i=1;i<=sheet.getLastRowNum();i++) {
            HSSFRow row = sheet.getRow(i);
            HSSFCell accountName = row.getCell((short)0);
            HSSFCell targetName = row.getCell((short)1);

            String targetNameErrMsg = "Target Name at row # '" + i + "' is empty!";
            if (targetName == null) {
                throw new Exception(targetNameErrMsg);
            }
            else if (targetName.toString().length() < 1) {
                throw new Exception(targetNameErrMsg);
            }
                            
            String accountErrMsg = "Account Name at row # '" + i + "' is empty!";
            if (accountName == null) {
                throw new Exception(accountErrMsg);
            }
            else if (accountName.toString().length() < 1) {
                throw new Exception(accountErrMsg);
            }
            

            System.out.println("Row("+i+") - Account: '" + accountName.toString() + "', On Target: '" + targetName.toString() + "'");
            velo.importer.ImportAccount ia = new velo.importer.ImportAccount();
            ia.setResourceName(targetName.toString());
            ia.setAccountName(accountName.toString());
            
            this.add(ia);
            
        }
    }
    
}
