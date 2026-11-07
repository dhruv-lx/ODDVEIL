import java.util.*;
class Marbel{
    public static void main(String args[]){
        Scanner in=new Scanner (System.in);
        System.out.println("Hello");
        System.out.println("Hello I, am leo whould you like to play marbel game with me?");
        System.out.println("Enter your name:");
        String name=in.nextLine();
        System.out.println("Hello "+name+"! I am glad to play with you.");
        int X=10;
        int leo=10;
        Random random = new Random();
        while(X>0 && leo>0){
        System.out.println("You have"+X+"Marbel left");
        System.out.println("How many marbel do you want to bet?");
        int bet=in.nextInt();
        if(bet>X){
            System.out.println("You don't have enough marbel to bet that much!");
            continue;
        }
        else {
            if(bet<0){
                System.out.println("You can't bet negative marbel!");
                continue;
            }
        }
        int hidden= random.nextInt(leo)+1;
        System.out.println("Guess (ODD=1/EVEN=2)");
        int guess=in.nextInt();
        int comp=hidden%2;
        if((guess==1&&comp==1)||(guess==2&&comp==0)){
            
            System.out.println("Correct guess! You win "+bet+"+ marbel!");
            X+=bet;
            leo-=bet;
        }
        else{
            System.out.println("Wrong guess! You lose "+bet+" marbel!");
            X-=bet;
            leo+=bet;
        }
        //game over condition
        if(X==0){
            System.out.println("Game over! You have no marbel left.");
            break;
        }else if(leo==0){
            System.out.println("Congratulations! Leo has no marbel left. You win!");
            break;
        }
    }   
        }}