package fileutility;

public class File {

    private String name;

    private String fileType;

    private String base64;

    public File() {
    }

    public File(String name, String fileType, String base64) {
        this.name = name;
        this.fileType = fileType;
        this.base64 = base64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
