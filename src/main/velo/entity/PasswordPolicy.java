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
package velo.entity;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.exceptions.PasswordValidationException;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordCharacterRule;
import edu.vt.middleware.password.PasswordChecker;
import edu.vt.middleware.password.PasswordException;
import edu.vt.middleware.password.PasswordLengthRule;
import edu.vt.middleware.password.PasswordSequenceRule;
import edu.vt.middleware.password.PasswordUserIDRule;

/**
 * An entity that represents a Password Policy
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("passwordPolicy")

@Table(name="VL_PASSWORD_POLICY")
@Entity
@SequenceGenerator(name="PasswordPolicyIdSeq",sequenceName="PASSWORD_POLICY_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "passwordPolicy.findById",query = "SELECT object(passwordPolicy) FROM PasswordPolicy passwordPolicy WHERE passwordPolicy.passwordPolicyId = :passwordPolicy"),
    @NamedQuery(name = "passwordPolicy.findByUniqueName",query = "SELECT object(passwordPolicy) FROM PasswordPolicy passwordPolicy WHERE passwordPolicy.uniqueName = :uniqueName"),
    @NamedQuery(name = "passwordPolicy.findAll", query = "SELECT object(passwordPolicy) FROM PasswordPolicy passwordPolicy")
})
public class PasswordPolicy extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    /**
     * A primary ID of the entity
     */
    private Long passwordPolicyId;
    
    /**
     * The name of the entity
     */
    private String uniqueName;
    
    /**
     * The display name of the entity
     */
    private String displayName;
    
    private String description;
    
    private int minLength = 6;
    
    private int maxLength = 10;
    
    private int maxFailuresBeforeLock = 5;
    
    private int minSpecialChars = 0;
    
    private int minNumericChars = 1;
    
    private String notAllowedChars;
    
    private String regExp;
    
    private String specialChars = "!@#$%^&*(),~[]{}:><.;'";
    
    private boolean passwordMayContainAccountName = true;
    
    private boolean allowSequences = true;
    
    
    /**
     * Set the ID of the entity
     * @param passwordPolicyId The ID of the entity to set
     */
    public void setPasswordPolicyId(Long passwordPolicyId) {
        this.passwordPolicyId = passwordPolicyId;
    }
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity to get
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_PASSWORD_POLICY_GEN",sequenceName="IDM_PASSWORD_POLICY_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_PASSWORD_POLICY_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="PasswordPolicyIdSeq")
    //@GeneratedValue //JB
    @Column(name="PASSWORD_POLICY_ID")
    public Long getPasswordPolicyId() {
        return passwordPolicyId;
    }
    
    /**
     * Set the unique name of the password policy
     * @param name The unique name of the password policy
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    /**
     * Get the uniqueName of unique name of the password policy
     * @return The uniqueName of unique name of the password policy
     */
    @Length(min=3, max=40) @NotNull //seam
    @Column(name="UNIQUE_NAME", nullable=false)
    public String getUniqueName() {
        return uniqueName;
    }
    
    /**
     * @return the displayName
     */
    @Column(name="DISPLAY_NAME", nullable=false)
    @Length(min=3, max=40) @NotNull //seam
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * @return the description
     */
    @Column(name="DESCRIPTION", nullable=true)
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return the maxFailuresBeforeLock
     */
    @Column(name="MAX_FAILURES_BEFORE_LOCK", nullable=false)
    @NotNull
    public int getMaxFailuresBeforeLock() {
        return maxFailuresBeforeLock;
    }
    
    /**
     * @param maxFailuresBeforeLock the maxFailuresBeforeLock to set
     */
    public void setMaxFailuresBeforeLock(int maxFailuresBeforeLock) {
        this.maxFailuresBeforeLock = maxFailuresBeforeLock;
    }
    
    /**
     * @return the maxLength
     */
    @Column(name="MAX_LENGTH", nullable=false)
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    /**
     * @return the minLength
     */
    @Column(name="MIN_LENGTH", nullable=false)
    @NotNull
    public int getMinLength() {
        return minLength;
    }
    
    /**
     * @param minLength the minLength to set
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }
    
    /**
     * @return the regExp
     */
    @Column(name="REG_EXP", nullable=true)
    public String getRegExp() {
        return regExp;
    }
    
    /**
     * @param regExp the regExp to set
     */
    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }
    
    /**
     * @param specialChars the specialChars to set
     */
    public void setSpecialChars(String specialChars) {
        this.specialChars = specialChars;
    }
    
    /**
     * @return the specialChars
     */
    @Column(name="SPECIAL_CHARS", nullable=true)
    public String getSpecialChars() {
        return specialChars;
    }
    
    /**
     * @param passwordMayContainAccountName the passwordMayContainAccountName to set
     */
    public void setPasswordMayContainAccountName(boolean passwordMayContainAccountName) {
        this.passwordMayContainAccountName = passwordMayContainAccountName;
    }
    
    /**
     * @return the passwordMayContainAccountName
     */
    //@Column(name="PASSWORD_MAY_CONTAIN_ACCOUNT_NAME", nullable=false)
    @Column(name="PWD_MAY_CONTAIN_ACCOUNT_NAME", nullable=false)
    public boolean isPasswordMayContainAccountName() {
        return passwordMayContainAccountName;
    }
    
    /**
     * @return the minNumericChars
     */
    @Column(name="MIN_NUMERIC_CHARS", nullable=false)
//    @NotNull
    public int getMinNumericChars() {
        return minNumericChars;
    }
    
    /**
     * @param minNumericChars the minNumericChars to set
     */
    public void setMinNumericChars(int minNumericChars) {
        this.minNumericChars = minNumericChars;
    }
    
    /**
     * @return the minSpecialChars
     */
    @Column(name="MIN_SPECIAL_CHARS", nullable=false)
//    @NotNull
    public int getMinSpecialChars() {
        return minSpecialChars;
    }
    
    /**
     * @param minSpecialChars the minSpecialChars to set
     */
    public void setMinSpecialChars(int minSpecialChars) {
        this.minSpecialChars = minSpecialChars;
    }
    
    /**
     * @param notAllowedChars the notAllowedChars to set
     */
    public void setNotAllowedChars(String notAllowedChars) {
        this.notAllowedChars = notAllowedChars;
    }
    
    /**
     * @return the notAllowedChars
     */
    @Column(name="NOT_ALLOWED_CHARS", nullable=true)
    public String getNotAllowedChars() {
        return notAllowedChars;
    }
    
        /*
        public boolean validate(String password, Account account) throws PasswordValidationException {
                String pattern;
         
                //First check length of the password
                if (password.length() < getMinLength()) {
                        throw new PasswordValidationException("The specified password is shorter than allowed length, password minimum length is '" + getMinLength() + "' characters.");
                }
         
                if (password.length() > getMaxLength()) {
                        throw new PasswordValidationException("The specified password is larger than allowed maximum length, password maximum length is '" + getMaxLength() + "' characters.");
                }
         
         
         
                try {
                        //Check minimum special chars
                        pattern = "[" + getSpecialChars() + "]";
                        int numOfSpecialChars = regExpOccurancesNumber(pattern,password);
                        if (numOfSpecialChars < getMinSpecialChars()) {
                                throw new PasswordValidationException("The Specified password does not contain minimum special characters, minimum special chars number is: '" + getMinSpecialChars() +"' , while specified only '" + numOfSpecialChars + "' special chars, special chars are: '" + getSpecialChars() + "'");
                        }
                }
                catch (PatternSyntaxException pse) {
                        throw new PasswordValidationException("Could not validate password, a RegExp Pattern Syntax exception has occured, detailed message: " + pse.getMessage());
                }
         
                try {
                        //Check minimum digits
                        pattern = "[0-9]";
                        int numOfNumericChars = regExpOccurancesNumber(pattern,password);
                        if (numOfNumericChars < getMinNumericChars()) {
                                throw new PasswordValidationException("The Specified password does not contain minimum digits, minimum digits is: '" + getMinNumericChars() +"' , while specified only '" + numOfNumericChars + "' digits");
                        }
                }
                catch (PatternSyntaxException pse) {
                        throw new PasswordValidationException("Could not validate password, a RegExp Pattern Syntax exception has occured, detailed message: " + pse.getMessage());
                }
         
         
                try {
                        //Check if the password contains not allowed chars
                        pattern = "[" + getNotAllowedChars() + "]";
                        int numOfNotAllowedChars = regExpOccurancesNumber(pattern,password);
                        if (numOfNotAllowedChars > 0) {
                                throw new PasswordValidationException("The Specified password contains not allowed characters, not allowd chars are: '" + getNotAllowedChars() + "'");
                        }
                }
                catch (PatternSyntaxException pse) {
                        throw new PasswordValidationException("Could not validate password, a RegExp Pattern Syntax exception has occured, detailed message: " + pse.getMessage());
                }
         
         
                if (!isPasswordMayContainAccountName()) {
                        if (password.toLowerCase().contains(account.getName().toLowerCase())) {
                                throw new PasswordValidationException("The specified password contains the account name in it, which is not allowed.");
                        }
                }
         
         
                return true;
                /*
                //Check if password contains not allowed chars
                //testString=testString.toLowerCase(); // make sure case is correct
                for (int i=0;i<password.length();i++) {
                        char c = password.charAt(i);
                        if (getNotAllowedChars().indexOf(c)!= -1) {
                                throw new PasswordValidationException("The specified password contains an invalid char '" + c + "at position '" + i + "'");
                        }
                }
         */
    
    //What about checking minimum
    //String regExpPolicy = "";
    //}
    
    
    /**
     * @param allowSequences the allowSequences to set
     */
    public void setAllowSequences(boolean allowSequences) {
        this.allowSequences = allowSequences;
    }
    
    /**
     * @return the allowSequences
     */
//    @Column(name="ALLOW_SEQUENCES", nullable=true)
    public boolean isAllowSequences() {
        return allowSequences;
    }
    
    public boolean validate(String password, String accountName) throws PasswordValidationException {
        try {
            getPasswordChecker(accountName).checkPassword(new Password(password));
            return false;
        } catch (PasswordException e) {
            throw new PasswordValidationException(e);
        }
    }
    
    
    
    
    
    
    
    
    @Transient
    public PasswordChecker getPasswordChecker(String accountName) {
        PasswordLengthRule  plr = new PasswordLengthRule(getMinLength(),getMaxLength()); // password must be between x and y chars long
        PasswordCharacterRule pcr = new PasswordCharacterRule();
        pcr.setNumberOfDigits(getMinNumericChars()); // require at least X digit in passwords
        //pcr.setNumberOfAlphabetical(0); //// require at least 0 alphabetical chars
        //pcr.setNumberOfNonAlphanumeric //// require at least 1 non-alphanumeric char
        //pcr.setNumberOfUppercase(0) //require at least x upper case chars
        //pcr.setNumberOfLowercase(0) //require at least x lower case chars
        //charRule.setNumberOfCharacteristics(3); // require at least 3 of the previous rules be met
        
        PasswordChecker checker = new PasswordChecker();
        checker.addPasswordRule(plr);
        //checker.addPasswordRule(whitespaceRule);
        checker.addPasswordRule(pcr);
        //checker.addPasswordRule(dictRule);
        //checker.addPasswordRule(seqRule);
        
        if (!isPasswordMayContainAccountName()) {
            PasswordUserIDRule puir = new PasswordUserIDRule(accountName);
            checker.addPasswordRule(puir);
        }
        
        if (!isAllowSequences()) {
            PasswordSequenceRule psr = new PasswordSequenceRule();
            checker.addPasswordRule(psr);
        }
        
        return checker;
    }
    
    
    /**
     * Find the number of matches for the specified regExp pattern
     * For instance the string 'abc!de$fg%' for pattern "[!$%]" will result 3 occurances.
     * @param stringPattern
     * @param content
     * @return
     */
    public int regExpOccurancesNumber(String stringPattern, String content) throws java.util.regex.PatternSyntaxException {
        //String content = "abcd@ef!!asdfsadf234skdfsadfasdf#";
        //Pattern pattern = Pattern.compile("[!@#$%^]");
        
        Pattern pattern = Pattern.compile(stringPattern);
        Matcher matcher = pattern.matcher(content);
        
        boolean found = false;
        int times = 0;
        while (matcher.find()) {
            //String currMacro = content.substring(matcher.start(), matcher.end());
            System.out.println(matcher.start() + "-" + matcher.end());
            found = true;
            times++;
        }
        
        if(!found){
            //System.out.println("No match found!");
        }
        
        //System.out.println("Found pattern: '" + times + "' times.");
        
        return times;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof PasswordPolicy))
            return false;
        PasswordPolicy ent = (PasswordPolicy) obj;
        if (this.passwordPolicyId.equals(ent.passwordPolicyId))
            return true;
        return false;
    }
    
}
