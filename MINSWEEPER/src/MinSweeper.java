import java.util.*;

import java.io.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene ;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane ;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;



public class MinSweeper extends Application {
    public  int difficulty=2 ;
    public  int mines_num=0;
    private int time=0 ;
    public  int super_mine=0 ;
    public  int num_matrix;
    public  int num_of_mines=16 ;
    private int open_tiles_counter=0;
    private Tile[][] grid ;
    private Scene scene ;
    private int flagnumber=0 ;
    private int round=0;
    ComboBox<String> ApplicationBox ,DetailsBox ;
    public static Label static_label ;
    private int WinnerOfGame=0;
    private static int [][] StatsOfGames = new int [5][4];
    private int GameNumber = 0;
    private int parametres[] =   {0,0,0,0};
    private long startTime ;
    private long endTime ;


    public  void load (String st ) throws Exception
    {
        File file =new File(st) ;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String  st1 ;
        int i=0;
        
        while((st1=br.readLine()) != null){
            int t=Integer.parseInt(st1);
            parametres[i]=t;
            i++ ;
        }
        if(i<4){
            br.close();
            System.out.println("Invalid Description");
            //throw new Exception("Invalid Description");
            return ;
        }
        br.close();
        
        if(parametres[0]> 2 || parametres[0]<0){
            System.out.println("Invalid range ");
            return ;
        }
    
        if(parametres[0]==1){
            // test changes
            if(parametres[1] <9 || parametres[1] >11 || parametres[2]<120 || parametres[2]>180 || parametres[3]!=0 ){
                System.out.println("Invalid range ");
                return ;
            }
        }
        else if(parametres[0]==2){
            if(parametres[1] < 35 || parametres[1] >45 || parametres[2] < 240 || parametres[2]>360 || parametres[3]>1|| parametres[3]<0 ){
                System.out.println("Invalid range ");
                return ;
            }
        } 

        difficulty=parametres[0] ;
        mines_num=parametres[1] ;
        time=parametres[2] ;
        super_mine=parametres[3] ;
        
    }




    public void CreateFile() {
          try {
            File myfile = new File("mines.txt");
            if (myfile.createNewFile()) {
              System.out.println("File created: " + myfile.getName());
            } else {
              System.out.println("File already exists.");
            }
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
      }
    

    public  void fieldinitializer(){
        
        Random random_mines= new Random();
        if (difficulty==1){
            num_matrix =9;
            num_of_mines = random_mines.nextInt(2)+9;
            grid =new Tile [9][9];
            
        }
        else {
            num_matrix=16;
            num_of_mines = random_mines.nextInt(10)+35;
            grid =new Tile [16][16];
            
        } 
    }

    public void setupField()
    {   
        int var=0;
        Random random1 = new Random();
        int randomsupermine= random1.nextInt(num_of_mines);
        try{
            FileWriter file = new FileWriter("mines.txt"); 
            while(var!=num_of_mines)
            {
                Random random = new Random();
                int i = random.nextInt(num_matrix);
                int j = random.nextInt(num_matrix);
                if(!grid[i][j].hasMine){
                    grid[i][j].hasMine=true;
                    grid[i][j].text.setText("X");
                    // grid[i][j].border.setFill(Color.BLUE); // to visual the bombs
                    String ist=String.valueOf(i);
                    String jst=String.valueOf(j);   
                    if(var==randomsupermine && difficulty==2){
                        grid[i][j].isSupermine=true;
                        grid[i][j].text.setText("SX");
                        file.write(ist+","+jst+","+"1\n");
                    } 
                    else{
                        file.write(ist+","+jst+","+"0\n");
                    }
                    var++;
                }
        }
        file.close();
    }catch (IOException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
      }
    }

    private Parent createContent(){
        fieldinitializer();
        Pane root = new Pane();
        root.setPrefSize(num_matrix*40,num_matrix*40); 
        for(int y=0; y<num_matrix; y++){
            for(int x=0;x<num_matrix;x++){
                Tile tile =new Tile(x, y,false,false,false);
                grid[x][y]=tile;
                root.getChildren().add(tile);
            }
        }
        setupField();

        for(int y=0; y<num_matrix; y++){
            for(int x=0;x<num_matrix;x++){
                Tile tile = grid[x][y];
                if (tile.hasMine) continue;
                long mines= getNeghbhors(tile).stream().filter(t->t.hasMine).count();
                if(mines>0) tile.text.setText(String.valueOf(mines) );
                
            }
        }
        root.relocate(0,80);
        Pane GUI = new Pane();
        GUI.setPrefSize(num_matrix*40,(num_matrix+2)*40); 

        Timepane timer =new Timepane(time);
        Menu menu = new Menu();
        
        //Create Menu Bar

        ApplicationBox = new ComboBox<>();   
        String [] menuchoices = {"APPLICATION","Create","Load","Start","Exit"} ;
        ApplicationBox.getItems().addAll(menuchoices);
        ApplicationBox.setValue("APPLICATION");
        ApplicationBox.setOnAction(e->MenuAction());

        DetailsBox =new ComboBox<>();
        DetailsBox.relocate(120,0);
        String [] Detailshoices = {"DETAILS","Rounds","Solution"} ;
        DetailsBox.getItems().addAll(Detailshoices);
        DetailsBox.setValue("DETAILS");
        DetailsBox.setOnAction(e->DetailsAction());

        GUI.getChildren().addAll(root,menu,timer,ApplicationBox,DetailsBox);
        return GUI;
        
    }


    private void MenuAction(){
        if(ApplicationBox.getValue()=="Exit"){
            System.exit(0 );
        }
        else if(ApplicationBox.getValue()=="Start"){
            open_tiles_counter=0;
            flagnumber=0;
            round=0;
            scene.setRoot(createContent());
            startTime=System.currentTimeMillis() / 1000;;
            return;
        }
        else if(ApplicationBox.getValue()=="Load"){
            LoadGame();
        }
        else if(ApplicationBox.getValue()=="Create"){
            CreateGame();
        }
    }
    public void CreateGame(){
        Stage popupwindow=new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("CREATE A SCENARIO");
        Label label1= new Label("SCENARIO-ID");
        TextField textField1 = new TextField();
        Label label2= new Label("Difficulty");
        TextField textField2 = new TextField();
        Label label3= new Label("Number of mines");
        TextField textField3 = new TextField();
        Label label4= new Label("Game Time");
        TextField textField4 = new TextField();
        Label label5= new Label("Has SuperMine");
        TextField textField5 = new TextField();
        Button button1= new Button("Creat");
        button1.setOnAction((new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String mytext= textField1.getText().toString();
                int a=Integer.parseInt( textField2.getText());
                int b=Integer.parseInt( textField3.getText());
                int c=Integer.parseInt( textField4.getText());
                int d=Integer.parseInt( textField5.getText());
                CreateGameFile(mytext,a,b,c,d);
                popupwindow.close();
            } ;
        }));
        VBox layout= new VBox(10);
        layout.getChildren().addAll(label1,textField1,label2,textField2,label3,textField3,label4,textField4,label5,textField5, button1);
        Scene scene1= new Scene(layout, 300, 400); 
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
        
        
    }
    
    public void CreateGameFile(String text, int game_difficulty , int mines, int timme,int supermine){
        
        try {
                if(game_difficulty==1){
                    if(mines>=9 && mines<=11 && timme>=120 && timme<=180 && supermine==0){
                        File myfile = new File("medialab"+File.separator+text+".txt");
                        System.out.println("File created: " + myfile.getName());
                        FileWriter file = new FileWriter(myfile);
                        if (myfile.createNewFile()){
                            String dif=String.valueOf(game_difficulty);
                            String min=String.valueOf(mines);
                            String tim=String.valueOf(timme);
                            String sup=String.valueOf(supermine);
                            file.write(dif+"\n"+min+"\n"+tim+"\n"+sup);
                            file.close();
                        }
                        else{
                            System.out.println("File already exists.");
                        }
                    }
                    else{
                        System.out.println("Wrong Value");
                    }
                }
                else if(game_difficulty==2){
                    if(mines>=35 && mines<=45 && timme>=240 && timme<=360 ){
                        File myfile = new File("medialab"+File.separator+text+".txt");
                        System.out.println("File created: " + myfile.getName());
                        FileWriter file = new FileWriter(myfile);
                        if (myfile.createNewFile()){
                            String dif=String.valueOf(game_difficulty);
                            String min=String.valueOf(mines);
                            String tim=String.valueOf(timme);
                            String sup=String.valueOf(supermine);
                            file.write(dif+"\n"+min+"\n"+tim+"\n"+sup);
                            file.close();
                        }
                    }
                    else{
                        System.out.println("File already exists");
                        }
                } 
            else {
                System.out.println("Wrong Value");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
    }
    public void LoadGame(){
        Stage popupwindow=new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("LOAD A GAME");
        Label label1= new Label("SCENARIO-ID");
        TextField textField1 = new TextField();
        Button button1= new Button("LOAD");
        button1.setOnAction((new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String mytext= textField1.getText().toString();
                try {
                    load( "medialab"+File.separator+mytext +".txt" ) ;
                    popupwindow.close();
                } catch (Exception e) {
                    System.out.println("File not exists");
                    popupwindow.close();
                } 
            } ;
        }));
        VBox layout= new VBox(10);
        layout.getChildren().addAll(label1,textField1,button1);
        Scene scene1= new Scene(layout, 300, 400); 
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();

    }
    private void DetailsAction() {
        if(DetailsBox.getValue()=="Solution"){
            for(int i=0;i<num_matrix;i++){
                for(int j=0;j<num_matrix;j++){
                    if(grid[i][j].hasMine){
                        grid[i][j].text.setVisible(true);
                        grid[i][j].border.setFill(Color.RED);
                    }
                }
            }
            endTime=System.currentTimeMillis() / 1000;;
            StatsOfGames[GameNumber%5][0]=num_of_mines;
            StatsOfGames[GameNumber%5][1]=round;
            StatsOfGames[GameNumber%5][2]=(int)(endTime-startTime);
            StatsOfGames[GameNumber%5][3]=WinnerOfGame;
            GameNumber++ ;
            WinnerOfGame=0;
            System.out.println("Game Over");
            
        }
        else if(DetailsBox.getValue()=="Rounds"){
            Rounds();
        }

    }
    public void Rounds(){
        Stage popupwindow=new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("Rounds");
        Label label01= new Label("ROUND");
        Label label02= new Label("MINES");
        Label label03= new Label("ATEMPTS");
        Label label04= new Label("TIME");
        Label label05= new Label("WINER(1 Player/0 PC)");
        HBox hBox = new HBox();
        hBox.getChildren().addAll(label01,label02,label03,label04,label05);
        hBox.setSpacing(15);
        Label label10= new Label("Round 1");
        Label label11=new Label(String.valueOf(StatsOfGames[0][0]));
        Label label12=new Label(String.valueOf(StatsOfGames[0][1]));
        Label label13=new Label(String.valueOf(StatsOfGames[0][2]));
        Label label14=new Label(String.valueOf(StatsOfGames[0][3]));
        HBox hBox1 = new HBox();
        hBox1.getChildren().addAll(label10,label11,label12,label13,label14);
        hBox1.setSpacing(40);
        Label label20= new Label("Round 2");
        Label label21=new Label(String.valueOf(StatsOfGames[1][0]));
        Label label22=new Label(String.valueOf(StatsOfGames[1][1]));
        Label label23=new Label(String.valueOf(StatsOfGames[1][2]));
        Label label24=new Label(String.valueOf(StatsOfGames[1][3]));
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(label20,label21,label22,label23,label24);
        hBox2.setSpacing(40);
        Label label30= new Label("Round 3");
        Label label31=new Label(String.valueOf(StatsOfGames[2][0]));
        Label label32=new Label(String.valueOf(StatsOfGames[2][1]));
        Label label33=new Label(String.valueOf(StatsOfGames[2][2]));
        Label label34=new Label(String.valueOf(StatsOfGames[2][3]));
        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(label30,label31,label32,label33,label34);
        hBox3.setSpacing(40);
        Label label40= new Label("Round 4");
        Label label41=new Label(String.valueOf(StatsOfGames[3][0]));
        Label label42=new Label(String.valueOf(StatsOfGames[3][1]));
        Label label43=new Label(String.valueOf(StatsOfGames[3][2]));
        Label label44=new Label(String.valueOf(StatsOfGames[3][3]));
        HBox hBox4 = new HBox();
        hBox4.getChildren().addAll(label40,label41,label42,label43,label44);
        hBox4.setSpacing(40);
        Label label50= new Label("Round 5");
        Label label51=new Label(String.valueOf(StatsOfGames[4][0]));
        Label label52=new Label(String.valueOf(StatsOfGames[4][1]));
        Label label53=new Label(String.valueOf(StatsOfGames[4][2]));
        Label label54=new Label(String.valueOf(StatsOfGames[4][3]));
        HBox hBox5 = new HBox();
        hBox5.getChildren().addAll(label50,label51,label52,label53,label54);
        hBox5.setSpacing(40);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox,hBox1,hBox2,hBox3,hBox4,hBox5);
        Scene scene= new Scene(vBox, 300, 400); 
        popupwindow.setScene(scene);
        popupwindow.showAndWait();

          
    }


    private class Timepane extends Pane{
        private Timeline timeline;
        private IntegerProperty timeSeconds = null;
        private Label timerLabel = new Label();
        Timepane(int time)
         {
            
            final Integer STARTTIME = time ;
            timeSeconds= new SimpleIntegerProperty(STARTTIME);
            timerLabel.textProperty().bind(timeSeconds.asString());
            timerLabel.setTextFill(Color.RED);
            timerLabel.setStyle("-fx-alignment: center; -fx-font-size:4em;-fx-background-color: black; ");
            timerLabel.relocate(num_matrix*30,0);
            timeSeconds.set(time);
            timeline = new Timeline();
            javafx.util.Duration duration = new javafx.util.Duration((STARTTIME)*1000);
            timeline.getKeyFrames().add(
                        new KeyFrame(duration,
                        new KeyValue(timeSeconds, 0)));
            timeline.playFromStart();
            getChildren().addAll(timerLabel);
        }
        
    }
    private class Menu extends Pane{
        private Rectangle menubar= new Rectangle(num_matrix*40,80);
        private Paint paint= Color.GRAY;
        private VBox information = new VBox() ;
        Label MinesOnGame = new Label("Mines: "+String.valueOf(num_of_mines));
        Label FlagOnGame = new Label("Marked Mines: "+String.valueOf(flagnumber));
        public Menu(){
            static_label=FlagOnGame;
            MinesOnGame.setTextFill(Color.PURPLE);
            FlagOnGame.setTextFill(Color.PURPLE);
            MinesOnGame.setStyle("-fx-font-weight: bold");
            FlagOnGame.setStyle("-fx-font-weight: bold");
            menubar.setFill(paint);
            information.getChildren().addAll(MinesOnGame,FlagOnGame);
            information.getSpacing();
            information.relocate(0,40);
            getChildren().addAll(menubar,information);
        }
    }
    private void GameOver(){
            endTime=System.currentTimeMillis() / 1000;;
            StatsOfGames[GameNumber%5][0]=num_of_mines;
            StatsOfGames[GameNumber%5][1]=round;
            StatsOfGames[GameNumber%5][2]=(int)(endTime-startTime);
            StatsOfGames[GameNumber%5][3]=WinnerOfGame;
            GameNumber++ ;
            open_tiles_counter=0;
            flagnumber=0;
            round=0;
            scene.setRoot(createContent());
            startTime=System.currentTimeMillis() / 1000 ;
            return;
    }
    
    
    private class Tile extends StackPane{
         private int x,y ;
         private boolean hasMine, isSupermine=false, hasFlag=false;
         private boolean isOpen=false;

         private Rectangle border = new Rectangle(38,38);
         private Text text= new Text();
         private Paint paint = Color.GREY ;

         public Tile(int x, int y , boolean hasMine , boolean isSupermine, boolean hasFlag){
            this.x=x;
            this.y=y;
            this.hasMine=hasMine;
            this.isSupermine=isSupermine;
            this.hasFlag=hasFlag ;
            
            border.setStroke(Color.GOLD);
            border.setFill(paint);
            text.setVisible(false);
            
            

            getChildren().addAll(border,text);

            setTranslateX(x*40);
            setTranslateY(y*40);

            this.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    MouseButton button = event.getButton();
                    long currenttime=(System.currentTimeMillis()/1000)-startTime;
                    if((int) currenttime > time ){
                        System.out.println("Game Over");
                        WinnerOfGame=0;
                        GameOver();
                    }

                    if(button==MouseButton.PRIMARY){
                        endTime= System.currentTimeMillis() / 1000;
                        round++ ;
                        open();
                        checkWin();
                    }
                    else if(button==MouseButton.SECONDARY){
                        markflag();
                    }
                }
            });

         }
         public void markflag(){
            Paint flag= Color.RED;
            if(isOpen) return ;
            if(!this.hasFlag){
                if(flagnumber<num_of_mines){
                    if(this.isSupermine && round<=4){
                        flagnumber++;
                        static_label.setText("Marked Mines: "+String.valueOf(flagnumber));
                        Superminemode();
                    }
                    else{
                    this.border.setFill(flag);
                    this.hasFlag=true;
                    flagnumber++;
                    static_label.setText("Marked Mines: "+String.valueOf(flagnumber));
                    }
                }
                else return ;
                
            }
            else{
                this.border.setFill(paint);
                this.hasFlag=false;
                flagnumber--;
                static_label.setText("Marked Mines: "+String.valueOf(flagnumber));
            }
            
         }

         public void Superminemode(){
            for(int i=0;i<num_matrix;i++){
                grid[this.x][i].Uncovertiles();
                grid[i][this.y].Uncovertiles();;
            }
            
         }

         public void open(){
            if(isOpen) return ;
            isOpen = true ;
            text.setVisible(true);
            border.setFill(null);
            open_tiles_counter ++ ;
            if(hasFlag) {
                flagnumber--;
                static_label.setText("Marked Mines: "+String.valueOf(flagnumber));
            }
            if(this.hasMine) {
                System.out.println("Game Over");
                WinnerOfGame=0;
                GameOver();
            }
            if(text.getText().isEmpty()){
                getNeghbhors(this).forEach(Tile :: open);
            }
         }


         public void checkWin(){
            if (open_tiles_counter==num_matrix*num_matrix - num_of_mines){
                System.out.println("You Won");
                WinnerOfGame=1;
                GameOver();
            } 
        }  


        public void Uncovertiles(){
            if(isOpen) return ;
            text.setVisible(true);
            border.setFill(null);
            isOpen=true ;
            if(hasMine){
                num_of_mines -- ;
            }
            if(hasFlag) {
                flagnumber--;
                static_label.setText("Marked Mines: "+String.valueOf(flagnumber));
            }
            open_tiles_counter++ ;
        }
    }
    

    private List<Tile> getNeghbhors(Tile tile){
        List<Tile> neighbors = new ArrayList<>();
        int[] points = new int[]{
            -1,-1,
            -1,0,
            -1,1,
            1,-1,
            1,0,
            1,1,
            0,-1,
            0,0,
            0,1,
        };

        for(int i =0 ; i<points.length;i++){
            int dx=points[i];
            int dy=points[++i];

            int newX=tile.x +dx ;
            int newY=tile.y +dy ;

            if(newX>=0 && newX<num_matrix && newY>=0 && newY < num_matrix){
                neighbors.add(grid[newX][newY]);
            }
        }

        return neighbors;
    }


    @Override
    public void start(Stage stage ) throws Exception{
        scene = new Scene(createContent());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args ){
        launch(args);
        
    }
}