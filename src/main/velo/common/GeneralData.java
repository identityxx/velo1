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
package velo.common;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(APPLICATION)
@Name("generalData")
public class GeneralData {
	
	private List<SelectItem> countries;
	
	public List<SelectItem> getCountryListAsSelectItems() {
		if (countries == null) {
			 countries = new ArrayList<SelectItem>();
			 initCountries();
			 return countries;
		} else {
			return countries;
		}
	}
	
	
	public void initCountries() {
		addCountry("af","Afghanistan");
		addCountry("ai","Åland Islands");
		addCountry("al","Albania");
		addCountry("dz","Algeria");
		addCountry("as","American Samoa");
		addCountry("ad","Andorra");
		addCountry("ao","Angola");
		addCountry("aq","Antarctica");
		addCountry("ag","Antigua and Barbuda");
		addCountry("ar","Argentina");
		addCountry("am","Armenia");
		addCountry("aw","Aruba");
		addCountry("au","Australia");
		addCountry("at","Austria");
		addCountry("az","Azerbaijan");
		addCountry("bs","Bahamas");
		addCountry("bh","Bahrain");
		addCountry("bd","Bangladesh");
		addCountry("bb","Barbados");
		addCountry("by","Belarus");
		addCountry("be","Belgium");
		addCountry("bz","Belize");
		addCountry("bj","Benin");
		addCountry("bm","Bermuda");
		addCountry("bt","Bhutan");
		addCountry("bo","Bolivia");
		addCountry("ba","Bosnia and Herzegovina");
		addCountry("bw","Botswana");
		addCountry("bv","Bouvet Island");
		addCountry("br","Brazil");
		addCountry("io","British Indian Ocean Territory");
		addCountry("bn","Brunei Darussalam");
		addCountry("bg","Bulgaria");
		addCountry("bf","Burkina Faso");
		addCountry("bi","Burundi");
		addCountry("kh","Cambodia");
		addCountry("cm","Cameroon");
		addCountry("ca","Canada");
		addCountry("cv","Cape Verde");
		addCountry("ky","Cayman Islands");
		addCountry("cf","Central African Republic");
		addCountry("td","Chad");
		addCountry("cl","Chile");
		addCountry("cn","China");
		addCountry("cx","Christmas Island");
		addCountry("cc","Cocos (Keeling) Islands");
		addCountry("co","Colombia");
		addCountry("km","Comoros");
		addCountry("cd","Congo, the Democratic Republic of the");
		addCountry("cg","Congo");
		addCountry("ck","Cook Islands");
		addCountry("cr","Costa Rica");
		addCountry("ci","Cote d Ivoire");
		addCountry("hr","Croatia");
		addCountry("cu","Cuba");
		addCountry("cy","Cyprus");
		addCountry("cz","Czech Republic");
		addCountry("dk","Denmark");
		addCountry("dj","Djibouti");
		addCountry("dm","Dominica");
		addCountry("do","Dominican Republic");
		addCountry("ec","Ecuador");
		addCountry("eg","Egypt");
		addCountry("sv","El Salvador");
		addCountry("gq","Equatorial Guinea");
		addCountry("er","Eritrea");
		addCountry("ee","Estonia");
		addCountry("et","Ethiopia");
		addCountry("fk","Falkland Islands (Malvinas)");
		addCountry("fo","Faroe Islands");
		addCountry("fj","Fiji");
		addCountry("fi","Finland");
		addCountry("fr","France");
		addCountry("gf","French Guiana");
		addCountry("pf","French Polynesia");
		addCountry("tf","French Southern Territories");
		addCountry("ga","Gabon");
		addCountry("gm","Gambia");
		addCountry("ge","Georgia");
		addCountry("de","Germany");
		addCountry("gh","Ghana");
		addCountry("gi","Gibraltar");
		addCountry("gr","Greece");
		addCountry("gl","Greenland");
		addCountry("gd","Grenada");
		addCountry("gp","Guadeloupe");
		addCountry("gu","Guam");
		addCountry("gt","Guatemala");
		addCountry("gw","Guinea-Bissau");
		addCountry("gn","Guinea");
		addCountry("gy","Guyana");
		addCountry("ht","Haiti");
		addCountry("hm","Heard and McDonald Islands");
		addCountry("va","Holy See (Vatican City State)");
		addCountry("hn","Honduras");
		addCountry("hk","Hong Kong");
		addCountry("hu","Hungary");
		addCountry("is","Iceland");
		addCountry("in","India");
		addCountry("id","Indonesia");
		addCountry("ir","Iran");
		addCountry("iq","Iraq");
		addCountry("ie","Ireland");
		addCountry("il","Israel");
		addCountry("it","Italy");
		addCountry("jm","Jamaica");
		addCountry("jp","Japan");
		addCountry("jo","Jordan");
		addCountry("kz","Kazakhstan");
		addCountry("ke","Kenya");
		addCountry("ki","Kiribati");
		addCountry("kp","Korea (North)");
		addCountry("kr","Korea (South)");
		addCountry("kw","Kuwait");
		addCountry("kg","Kyrgyzstan");
		addCountry("la","Lao People s Democratic Republic");
		addCountry("lv","Latvia");
		addCountry("lb","Lebanon");
		addCountry("ls","Lesotho");
		addCountry("lr","Liberia");
		addCountry("ly","Libya");
		addCountry("li","Liechtenstein");
		addCountry("lt","Lithuania");
		addCountry("lu","Luxembourg");
		addCountry("mo","Macau");
		addCountry("mk","Macedonia");
		addCountry("mg","Madagascar");
		addCountry("mw","Malawi");
		addCountry("my","Malaysia");
		addCountry("mv","Maldives");
		addCountry("ml","Mali");
		addCountry("mt","Malta");
		addCountry("mh","Marshall Islands");
		addCountry("mq","Martinique");
		addCountry("mr","Mauritania");
		addCountry("mu","Mauritius");
		addCountry("yt","Mayotte");
		addCountry("mx","Mexico");
		addCountry("fm","Micronesia, Federated States of");
		addCountry("md","Moldova, Republic of");
		addCountry("mc","Monaco");
		addCountry("mn","Mongolia");
		addCountry("ms","Montserrat");
		addCountry("ma","Morocco");
		addCountry("mz","Mozambique");
		addCountry("mm","Myanmar");
		addCountry("na","Namibia");
		addCountry("nr","Nauru");
		addCountry("np","Nepal");
		addCountry("an","Netherlands Antilles");
		addCountry("nl","Netherlands");
		addCountry("nc","New Caledonia");
		addCountry("nz","New Zealand");
		addCountry("ni","Nicaragua");
		addCountry("ne","Niger");
		addCountry("ng","Nigeria");
		addCountry("nu","Niue");
		addCountry("nf","Norfolk Island");
		addCountry("mp","Northern Mariana Islands");
		addCountry("no","Norway");
		addCountry("om","Oman");
		addCountry("pk","Pakistan");
		addCountry("pw","Palau");
		addCountry("ps","Palestinian Territory, Occupied");
		addCountry("pa","Panama");
		addCountry("pg","Papua New Guinea");
		addCountry("py","Paraguay");
		addCountry("pe","Peru");
		addCountry("ph","Philippines");
		addCountry("pn","Pitcairn");
		addCountry("pl","Poland");
		addCountry("pt","Portugal");
		addCountry("pr","Puerto Rico");
		addCountry("qa","Qatar");
		addCountry("re","Reunion");
		addCountry("ro","Romania");
		addCountry("ru","Russia");
		addCountry("rw","Rwanda");
		addCountry("kn","Saint Kitts and Nevis");
		addCountry("lc","Saint Lucia");
		addCountry("vc","Saint Vincent and the Grenadines");
		addCountry("ws","Samoa");
		addCountry("sm","San Marino");
		addCountry("st","Sao Tome and Principe");
		addCountry("sa","Saudi Arabia");
		addCountry("sn","Senegal");
		addCountry("cs","Serbia and Montenegro");
		addCountry("sc","Seychelles");
		addCountry("sl","Sierra Leone");
		addCountry("sg","Singapore");
		addCountry("sk","Slovakia");
		addCountry("si","Slovenia");
		addCountry("sb","Solomon Islands");
		addCountry("so","Somalia");
		addCountry("za","South Africa");
		addCountry("gs","South Georgia and the South Sandwich Islands");
		addCountry("es","Spain");
		addCountry("lk","Sri Lanka");
		addCountry("sh","St. Helena");
		addCountry("pm","St. Pierre and Miquelon");
		addCountry("sd","Sudan");
		addCountry("sr","Suriname");
		addCountry("sj","Svalbard and Jan Mayen Islands");
		addCountry("sz","Swaziland");
		addCountry("se","Sweden");
		addCountry("ch","Switzerland");
		addCountry("sy","Syria");
		addCountry("tw","Taiwan");
		addCountry("tj","Tajikistan");
		addCountry("tz","Tanzania");
		addCountry("th","Thailand");
		addCountry("tl","Timor-Leste");
		addCountry("tg","Togo");
		addCountry("tk","Tokelau");
		addCountry("to","Tonga");
		addCountry("tt","Trinidad and Tobago");
		addCountry("tn","Tunisia");
		addCountry("tr","Turkey");
		addCountry("tm","Turkmenistan");
		addCountry("tc","Turks and Caicos Islands");
		addCountry("tv","Tuvalu");
		addCountry("ug","Uganda");
		addCountry("ua","Ukraine");
		addCountry("ae","United Arab Emirates");
		addCountry("gb","United Kingdom");
		addCountry("um","United States Minor Outlying Islands");
		addCountry("us","United States");
		addCountry("uy","Uruguay");
		addCountry("uz","Uzbekistan");
		addCountry("vu","Vanuatu");
		addCountry("ve","Venezuela");
		addCountry("vn","Viet Nam");
		addCountry("vg","Virgin Islands (British)");
		addCountry("vi","Virgin Islands (U.S.)");
		addCountry("wf","Wallis and Futuna Islands");
		addCountry("eh","Western Sahara");
		addCountry("ye","Yemen");
		addCountry("zm","Zambia");
		addCountry("zw","Zimbabwe");
	}
	
	public void addCountry(String shortcut,String display) {
		countries.add(new SelectItem(shortcut,display));
	}
}
