package MainChat;

import java.io.File;

public class  ServerGUI{
    public static void main(String[] args) {
        File directory = new File("\\file");
        File file = new File(directory.getAbsolutePath() + File.separatorChar + "1.txt");
        System.out.println(System.getProperty("user.dir")+"\\file");
        String[] files = file.list();
        for(String na:files){
            System.out.println(na+"\n");
        }
    }
}