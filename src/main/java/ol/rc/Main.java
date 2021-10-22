package ol.rc;

//TODO: make BlocksImageCompressor - IImageСompressor which will detect changed rectangle areas (blocks) to transfer them.
//TODO: draw changed rectangle areas (blocks) on Graphics, without create Image

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        LauncherV2 launcher= new LauncherV2();
        launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        launcher.setVisible(true);
        launcher.setSize(900,700);
        launcher.pack();


//        Launcher launcher=new Launcher();
//        launcher.setVisible(true);
//        launcher.pack();


//        Thread thread;
//        Runnable runnable=()->{
//            Launcher launcher=new Launcher();
//            launcher.pack();
//        };
//        thread=new Thread(runnable);
//        thread.setDaemon(true);
//        thread.start();
    }
}
