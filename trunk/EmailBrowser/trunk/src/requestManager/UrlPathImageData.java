package requestManager;

import java.io.IOException;

/**
 * 
 * UrlPathImageData stores all information needed to attach an image to an email
 * 
 */
public class UrlPathImageData {
	private final String originalName;

	private final String renamed;

	private byte[] imageDataBytes;

	/**
	 * Constructor -- sets up the original name, the renaming, and the image
	 * data
	 * 
	 * @param _originalName
	 *            original name of the image
	 * @param _renamed
	 *            the new name of the image
	 * @param _imageDataBytes
	 *            the actual image data
	 */
	public UrlPathImageData(String _originalName, String _renamed,
			byte[] _imageDataBytes) {
		originalName = _originalName;
		renamed = _renamed;
		imageDataBytes = _imageDataBytes;
	}

	/**
	 * If a byte array of image data is null, then this method fills in the byte
	 * array by reading the image data.
	 * 
	 * @return a byte array of image data
	 * @throws IOException
	 */
	public byte[] getImageData() throws IOException {
		return imageDataBytes;
	}

	/**
	 * 
	 * @return original name of the image
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * 
	 * @return extension of the original name of the image
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
	 * @return new name of the image
	 */
	public String getRenamed() {
		return renamed;
	}

}
