package g3dtools.preview;

import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetKey;
import com.g3d.asset.AssetLocator;
import com.g3d.asset.AssetManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSystemLocator implements AssetLocator {

    private File root;

    public void setRootPath(String rootPath) {
        if (rootPath == null)
            throw new NullPointerException();

        root = new File(rootPath);
        if (!root.isDirectory())
            throw new RuntimeException("Given root path not a directory");
    }

    private static class AssetInfoFile extends AssetInfo {

        private File file;

        public AssetInfoFile(AssetManager manager, AssetKey key, File file){
            super(manager, key);
            this.file = file;
        }

        @Override
        public InputStream openStream() {
            try{
                return new FileInputStream(file);
            }catch (FileNotFoundException ex){
                return null;
            }
        }
    }

    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        File file = new File(root, name);
        if (file.exists() && file.isFile()){
            return new AssetInfoFile(manager, key, file);
        }else{
            return null;
        }
    }

}
