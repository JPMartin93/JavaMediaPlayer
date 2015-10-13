/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jtunes;

/**
 *
 * @author Jesses
 */

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javax.swing.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.swing.filechooser.FileNameExtensionFilter;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class jTunes {

    /**
     * @param args the command line arguments
     */
       
    private static Timeline t;
    private static MediaPlayer jPod;
    private static MediaPlayer dataPlayer;
    private static Media jMedia;
    private static boolean hasSong = false;
    private static final ArrayList<File> Que = new ArrayList<>();
    private static int table = 0;
    private static final ObservableList<Song> QueList = FXCollections.observableArrayList();
    private static final ObservableList<Song> PrevList = observableArrayList();
    
    private static void initFX(JFXPanel panel)
    {
        Scene scene = createScene(); 
        panel.setScene(scene);
    }
    
    @SuppressWarnings("Convert2Lambda")
    private static void createTimer(Label elaps, Slider seek)
    {
       t = new Timeline(new KeyFrame(Duration.millis(10),new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent e)
                    {
                        elaps.setText(calcTime(jPod.getCurrentTime()));
                        double loc = jPod.getCurrentTime().toMinutes() / jPod.getStopTime().toMinutes();
                        seek.setValue(loc * seek.getMax());
                    }
                }));
    }
      
    private static Scene createScene()
    {
        Group root = new Group();
        Scene scene = new Scene(root,500,500);
        
        //Background work around
        Rectangle back = new Rectangle();
        back.setLayoutX(0);
        back.setLayoutY(0);
        back.setHeight(272);
        back.setWidth(295);
        back.getStyleClass().add("backgroundRect");
        
        scene.getRoot().getStyleClass().add("backGround");
        root.getChildren().add(back);
        
        //Style Sheet Import
        File StyleSheet = new File("jTunesStyleSheet.css"); 
        scene.getStylesheets().clear();
        
        try
        {
            scene.getStylesheets().add(StyleSheet.toURI().toURL().toString());
        }
        catch(Exception e)
        {
            System.out.println("Style Sheat Upload Error" + e.toString());
        }
       
        // Control Declaration
        Button btnLoad = new Button();
        Button btnPlay = new Button();
        Button btnPause = new Button();
        Button btnStop = new Button();
        Button btnEject = new Button();
        Button btnFF = new Button();
        Button btnRW = new Button();
        Slider seekBar = new Slider();
        Button Style = new Button();  
        TableView<Song> PlayList = new TableView();
        TableView SongData = new TableView();
        TableView PreviousSong = new TableView();
        ToggleButton tlgPlayList = new ToggleButton();
        ToggleButton tlgPreviousSong = new ToggleButton();
        ToggleButton tlgSongData = new ToggleButton();
        VBox SongBox = new VBox();
        VBox PrevSongBox = new VBox();
        
        Style.setText("ST_YLE");
        Style.setLayoutX(-200);
        Style.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent evt)
            {
                scene.getStylesheets().clear();
        
                try
                {
                    scene.getStylesheets().add(StyleSheet.toURI().toURL().toString());
                }
                catch(Exception e)
                {
                    System.out.println("Style Sheat Upload Error");
                }
            }
        });
        
        Label Name = new Label("Name Of Song");
        Name.setLayoutX(0);
        Name.setLayoutY(110);
        
        Label lblName = new Label("No Song In Player");
        lblName.getStyleClass().add("MetaData");
        lblName.setLayoutX(100);
        lblName.setLayoutY(110);
        
        Label CurrentTime = new Label("Current Time");
        CurrentTime.setLayoutX(0);
        CurrentTime.setLayoutY(210);
        
        seekBar.setValue(0);
        seekBar.setMax(300);
        seekBar.setOnMouseEntered(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent evt)
            {
                try
                {
                    seekBar.setTooltip(new Tooltip(calcTime(jPod.getCurrentTime())));
                }
                catch(Exception e)
                {}
            }
        });
        seekBar.setOnMouseDragged(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent evt)
            {
                double loc = seekBar.getValue() / seekBar.getMax();
                double playerloc = jPod.getStopTime().toMillis() * loc;
                Duration pl = new Duration(playerloc);
                jPod.seek(pl);
            }
        });
        seekBar.setLayoutX(75);
        seekBar.setLayoutY(210);
        seekBar.setOrientation(Orientation.HORIZONTAL);
        
        Label lblElapsedTime = new Label("00:00:00");
        lblElapsedTime.getStyleClass().add("TimeData");
        lblElapsedTime.setLayoutX(seekBar.getLayoutX() - 10);
        lblElapsedTime.setLayoutY(seekBar.getLayoutY() - 15);
        
        Label lblTotalLength = new Label("00:00:00");
        lblTotalLength.getStyleClass().add("TimeData");
        lblTotalLength.setLayoutX(seekBar.getLayoutX() + 110);
        lblTotalLength.setLayoutY(seekBar.getLayoutY() - 15);
         
        createTimer(lblElapsedTime,seekBar);
        
        btnLoad.setText("_Load");
        btnLoad.setTooltip(new Tooltip("Load a new song into the media player."));
        btnLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Song","mp3","m4a"));
                int val = chooser.showOpenDialog(null);
                if(val == JFileChooser.APPROVE_OPTION)
                {
                    File song = chooser.getSelectedFile();
                    String name = song.getName();
                    URI path = song.toURI();
                  
                    Que.add(song);
                    dataPlayer = new MediaPlayer(new Media(Que.get(Que.size()-1).toURI().toString()));
                    dataPlayer.setOnReady(() -> 
                    {
                        QueList.add(new Song(song.getName(),calcTime(dataPlayer.getStopTime())));
                    });
                   
                    
                    if(!hasSong)
                    {
                        jMedia = new Media(path.toString());
                        jPod = new MediaPlayer(jMedia);

                    }
                    
                      
                    jPod.setOnEndOfMedia(new Runnable(){
                        @Override
                        public void run()
                        {
                            Que.remove(0);
                            if(!Que.isEmpty()){
                            Runnable jPodOnReady = jPod.getOnReady();
                            jPod = new MediaPlayer(new Media(Que.get(0).toURI().toString())); 
                            lblName.setText(Que.get(0).getName());   
                            jPod.setOnEndOfMedia(this);  
                            jPod.setOnReady(jPodOnReady);
                            btnPlay.fire();
                            }
                            else
                                btnEject.fire();                            
                        }
                    });                   
                    jPod.setOnReady(new Runnable(){
                        @Override
                        public void run()
                        {
                            lblTotalLength.setText(calcTime(jPod.getStopTime()));
                        }
                    });
                    
                    hasSong = true;
                    
                    btnPlay.setDisable(false);
                    btnPause.setDisable(false);
                    btnStop.setDisable(false);
                    btnEject.setDisable(false);
                    btnFF.setDisable(false);
                    btnRW.setDisable(false);
                    
                    lblName.setText(name);
                    
                }  
            }
        });
        btnLoad.setLayoutX(230);
        btnLoad.setLayoutY(235);
        
        btnPlay.setText("_Play");
        btnPlay.setTooltip(new Tooltip("Press to play the current song."));
        btnPlay.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt)
            {
                if(hasSong)
                {
                    jPod.play();
                    t.setCycleCount(Animation.INDEFINITE);
                    t.play();
                }
            }
        });
        btnPlay.setLayoutX(10);
        btnPlay.setLayoutY(235);
        btnPlay.setDisable(true);
        
        btnPause.setText("P_ause");
        btnPause.setTooltip(new Tooltip("Press to pause the song."));
        btnPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt)
            {
                jPod.pause();
                t.stop();
            }
        });
        btnPause.setLayoutX(65);
        btnPause.setLayoutY(235);
        btnPause.setDisable(true);
        
        btnStop.setText("_Stop");
        btnStop.setTooltip(new Tooltip("Press to stop the song"));
        btnStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt)
            {
                jPod.stop();
                t.stop();              
            }
        });
        btnStop.setLayoutX(120);
        btnStop.setLayoutY(235);
        btnStop.setDisable(true);
        
        btnEject.setText("_Eject");
        btnEject.setTooltip(new Tooltip("Press to eject the curent playlist"));
        btnEject.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent evt)
            {
                jPod.stop();
                
                hasSong = false;
                
                btnPlay.setDisable(true);
                btnPause.setDisable(true);
                btnStop.setDisable(true);
                btnEject.setDisable(true);
                btnLoad.setDisable(false);
                btnFF.setDisable(true);
                btnRW.setDisable(true);
            }
        });
        btnEject.setLayoutX(175);
        btnEject.setLayoutY(235);
        btnEject.setDisable(true);
        
        btnFF.setText("_FF");
        btnFF.setTooltip(new Tooltip("Press to skip to the next song"));
        btnFF.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent evt)
            {
                jPod.stop();
                jPod.onEndOfMediaProperty().get().run();
            }
        });
        btnFF.setLayoutX(230);
        btnFF.setLayoutY(200);
        btnFF.setDisable(true);
        
        btnRW.setText("_RW");
        btnRW.setTooltip(new Tooltip("Press to rewind"));
        btnRW.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent evt)
            {
                if(jPod.getCycleDuration().toSeconds() > 5)
                {
                    jPod.stop();
                    jPod.play();
                }
                else
                {
                    jPod.stop();
                    
                    Runnable S = jPod.getOnStopped();
                    Runnable P = jPod.getOnPlaying();
                    Runnable E = jPod.getOnEndOfMedia();
                    
                   // jPod = new MediaPlayer(new Media(Previous.get(0).toURI().toString()));
                    jPod.setOnEndOfMedia(E);
                    jPod.setOnPlaying(P);
                    jPod.setOnStopped(S);
                }
            }
        });
        btnRW.setLayoutX(10);
        btnRW.setLayoutY(200);
        btnRW.setDisable(true);
        
        TableColumn SongName = new TableColumn("Song");
        SongName.setCellValueFactory(new PropertyValueFactory<>("name"));
        SongName.setMinWidth(230/2);
        SongName.setEditable(false);
        TableColumn SongLength = new TableColumn("Song Length");
        SongLength.setCellValueFactory(new PropertyValueFactory<>("songLength"));
        SongLength.setMinWidth(230/2);
        SongLength.setEditable(false);
        PlayList.getColumns().setAll(SongName,SongLength);
        PlayList.setLayoutX(50);
        PlayList.setLayoutY(20);
        PlayList.setMinSize(230, 170);
        PlayList.setMaxSize(230, 170);  
        PlayList.setItems(QueList);
        PlayList.setVisible(true);
        
        SongBox.setLayoutX(50);
        SongBox.setLayoutY(20);
        SongBox.setPrefSize(230,170);
        SongBox.getChildren().addAll(PlayList);
        
        
        TableColumn prevSongName = new TableColumn("Song");
        prevSongName.setCellValueFactory(new PropertyValueFactory<>("name"));
        prevSongName.setMinWidth(230/2);
        prevSongName.setEditable(false);
        TableColumn prevSongLength = new TableColumn("Song Length");
        prevSongLength.setCellValueFactory(new PropertyValueFactory<>("songLength"));
        prevSongLength.setMinWidth(230/2);
        prevSongLength.setEditable(false);
        PreviousSong.getColumns().setAll(prevSongName,prevSongLength);
        PreviousSong.setLayoutX(50);
        PreviousSong.setLayoutY(20);
        PreviousSong.setMinSize(230, 170);
        PreviousSong.setMaxSize(230, 170);     
        PreviousSong.setItems(PrevList);
        PreviousSong.setVisible(true);
        
        PrevSongBox.setLayoutX(50);
        PrevSongBox.setLayoutY(20);
        PrevSongBox.setPrefSize(230, 170);
        PrevSongBox.getChildren().addAll(PreviousSong);
        PrevSongBox.setVisible(false);
        
        ToggleGroup listGroup = new ToggleGroup();
        
        tlgPlayList.setText("Play\nL_ist");
        tlgPlayList.getStyleClass().add("tglButton");
        tlgPlayList.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent evt)
            {
                if(table == 0)
                {
                    tlgPlayList.setSelected(true);
                }
                else if(table != 0)
                {             
                    PrevSongBox.setVisible(false);
                    SongBox.setVisible(true);
                    table = 0;
                }
               
            }
        });
        tlgPlayList.setToggleGroup(listGroup);
        tlgPlayList.setLayoutX(11);
        tlgPlayList.setLayoutY(20);
        tlgPlayList.setSelected(true);
   
        tlgPreviousSong.setText("Last\nS_ong");
        tlgPreviousSong.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent evt)
            {
                if(table == 1)
                {
                    tlgPreviousSong.setSelected(true);
                }
                else if(table != 1)
                {
                    SongBox.setVisible(false);
                    PrevSongBox.setVisible(true);
                    table = 1;
                }
               
            }
        });
        tlgPreviousSong.getStyleClass().add("tglButton");
        tlgPreviousSong.setToggleGroup(listGroup);
        tlgPreviousSong.setLayoutX(11);
        tlgPreviousSong.setLayoutY(64);
                       
        //Adds Components
        root.getChildren().add(btnLoad);
        root.getChildren().add(btnPlay);
        root.getChildren().add(btnPause);
        root.getChildren().add(btnStop);
        root.getChildren().add(btnEject);
        root.getChildren().add(btnFF);
        root.getChildren().add(btnRW);
        root.getChildren().add(seekBar);
        root.getChildren().add(Style);
        root.getChildren().add(tlgPlayList);
        root.getChildren().add(tlgPreviousSong);
        root.getChildren().add(PrevSongBox);
        root.getChildren().add(SongBox);
        root.getChildren().add(lblElapsedTime);
        root.getChildren().add(lblTotalLength);
        
        return scene;
    }
    
    private static String calcTime(Duration d)
    {
       String Time;
       double TimeInMill = d.toMillis();
       int min = (int)(TimeInMill / (1000*60));
       TimeInMill -= min*1000*60;
       int sec = (int)(TimeInMill / 1000);
       TimeInMill -= sec * 1000;
       int mill = (int)(TimeInMill / 10);
       
       Time = String.format("%02d:%02d:%02d", min, sec,mill);
       
      return Time;
    }
    
    private static void init()
    {
        JFrame frame = new JFrame("jTunes");
        JFXPanel panel = new JFXPanel();
        frame.add(panel);
        frame.setSize(300,300);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        
        Platform.runLater(() -> initFX(panel));
    }
    
    public static void main(String[] args) 
    {
        // TODO code application logic here
        SwingUtilities.invokeLater(() -> init());     
    } 
    
    public static class Song 
    {
        private final String Name;
        private final String SongLength;
    
        private Song(String Name,String SongLength)
        {
        this.Name = Name;
        this.SongLength = SongLength;
    }       
    
        public String getName()
        {
        return Name;
    }
    
        public String getLength()
        {
            return SongLength;
        }
}
}