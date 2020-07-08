package TestTools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.Scanner;

public class CalTest {



    public static void main(String[] args){


        //day2

        String[]z=new String[2];
        z[0]="subway";
        z[1]="maccas";
        LocalTime[] t = new LocalTime[2];
        LocalTime[] u = new LocalTime[2];

        t[0] = LocalTime.of(10,1,4,6);
        u[0] = LocalTime.of(13,0);

        t[1] = LocalTime.of(14,0);
        u[1] = LocalTime.of(17,0);

        ScheduleDay day2 = new ScheduleDay();
        day2.setStartTimes(t);
        day2.setEndTimes(u);
        day2.setNames(z);
        day2.setupPixels();
        day2.setDate(LocalDate.of(2020,12,31));
        day2.printDay();


        //HALF IT FOR SCALE
        String [] n = new String[3];
        n[0]="eb games";
        n[1]="Hello there";
        n[2]="Welcome aa";

        LocalTime[] x = new LocalTime[3];
        LocalTime[] y = new LocalTime[3];
        int[]a = new int[3];
        int[]b = new int[3];

       // LocalTime start = LocalTime.of(8,0);

        x[0] = LocalTime.of(8,1,4,6);
        y[0] = LocalTime.of(9,0);

        x[1] = LocalTime.of(12,0);
        y[1] = LocalTime.of(13,0);

        x[2] = LocalTime.of(14,15);
        y[2] = LocalTime.of(19,15);

        ScheduleDay day1 = new ScheduleDay();
        day1.setStartTimes(x);
        day1.setEndTimes(y);
        day1.setNames(n);
        day1.setupPixels();
        day1.setDate(LocalDate.of(2013,3,20));
        day1.printDay();

        for(int i = 0; i<x.length;i++)
        {

            x[i]=x[i].truncatedTo(ChronoUnit.MINUTES);
            y[i]=y[i].truncatedTo(ChronoUnit.MINUTES);

            //lets get the minutes
            int h = x[i].getHour();
            h = h * 60;
            h = h + x[i].getMinute();
            //8:00 in minutes is 480;
            h = h - 480;

            int j = y[i].getHour();
            j = j * 60;
            j = j + y[i].getMinute();
            j = j - 480;

            //5 pm in minutes 1020


            //lets see if the start or finish are out of range
            //if they are then lets trim them
            if (h<0)
            {
                h=0;
            }
            if(j>540)
            {
                j=540;
            }

            System.out.println("start With offset applied:"+h);
            System.out.println("end With offset applied:"+j);


            a[i]=h;
            b[i]=j;


            System.out.println(" start"+x[i]);
            System.out.println(" end"+y[i]);

        }



        ScheduleDay[] week = new ScheduleDay[2];
        week[0]= day1;
        week[1]=day2;

        //lets draw it
        CGTemplate cal = new CGTemplate(week);

        //cal.drawDay1(a,b);
        //System.out.println(x[0]);

    }



}
