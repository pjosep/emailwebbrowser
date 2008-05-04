package requestManager;

/**
 * 
 * UrlPathImUrlPathCssDataageData stores all information needed to attach a css
 * page to an email
 * 
 */
public class UrlPathCssData {
	private final String originalName;

	private final String renamed;

	private final String cssData;

	/**
	 * Constructor -- sets up the original name, the renaming, and the CSS data
	 * 
	 * @param _originalName
	 *            original name of the CSS Page
	 * @param _renamed
	 *            the new name of the CSS Page
	 * @param _imageData
	 *            the actual CSS Page data
	 */
	public UrlPathCssData(String _originalName, String _renamed, String _cssData) {
		originalName = _originalName;
		cssData = _cssData;
		renamed = _renamed;
	}

	/**
	 * 
	 * @return original name of the CSS Page
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * 
	 * @return extension of the original name of the CSS Page
	 */
	public String getOriginalNameExt() {
		return (originalName.lastIndexOf(".") == -1) ? "" : originalName
				.substring(originalName.lastIndexOf(".") + 1, originalName
						.length());
	}

	/**
	 * 
	 * @return original name of the image without any path info
	 */
	public String getOriginalNameNoPath() {
		String slash;
		if ((originalName.contains("/")) && (originalName.contains("\\"))) {
			if (originalName.lastIndexOf("/") >= originalName.lastIndexOf("\\")) {
				slash = "/";
			} else {
				slash = "\\";
			}
		} else if (originalName.contains("/")) {
			slash = "/";
		} else if (originalName.contains("\\")) {
			slash = "\\";
		} else {
			return originalName;
		}
		return originalName.substring(originalName.lastIndexOf(slash) + 1,
				originalName.length());
	}

	/**
	 * 
	 * @return new name of the CSS Page
	 */
	public String getRenamed() {
		return renamed;
	}

	/**
	 * 
	 * @return css page data
	 */
	public String getCssData() {
		return cssData;
	}

}
