package velo.richfaces;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;

@Name("richEditorConf")
public class RichEditorConf {

	private String currentConfiguration = CONFIGS_PACKAGE + "simple";
	private String viewMode = "visual";
	private String value;
	private boolean liveUpdatesEnabled=false;
	
	private static final String CONFIGS_PACKAGE = "/";
	List<SelectItem> configurations = new ArrayList<SelectItem>();
	
	public RichEditorConf() {
		configurations.add(new SelectItem(CONFIGS_PACKAGE + "simple", "Simple"));
		configurations.add(new SelectItem(CONFIGS_PACKAGE + "advanced", "Advanced"));
	}
	
	public String getCurrentConfiguration() {
		return currentConfiguration;
	}

	public void setCurrentConfiguration(String currentConfiguration) {
		this.currentConfiguration = currentConfiguration;
	}

	public List<SelectItem> getConfigurations() {
		return configurations;
	}

	public String getViewMode() {
		return viewMode;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isLiveUpdatesEnabled() {
		return liveUpdatesEnabled;
	}

	public void setLiveUpdatesEnabled(boolean liveUpdatesEnabled) {
		this.liveUpdatesEnabled = liveUpdatesEnabled;
	}
	
	
	
}
