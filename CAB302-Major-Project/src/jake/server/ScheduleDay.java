package jake.server;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ScheduleDay implements Serializable {
    public int[]startPixels;
    public int[]endPixels;
    public String[]names;
    public LocalTime[]startTimes;
    public LocalTime[]endTimes;
    public LocalDate date;

    public void setDate(LocalDate date){
        this.date = date;
    }

    public void setNames(String[] names)
    {
        this.names = names;
    }
    public void setStartTimes(LocalTime[]startTimes)
    {
        this.startTimes = startTimes;
        System.out.println("start times given len:"+startTimes.length);
    }
    public void setEndTimes(LocalTime[]endTimes)
    {
        this.endTimes = endTimes;
        System.out.println("start times given len:"+endTimes.length);
    }
    public void setupPixels()
    {

        System.out.println("This day will have "+this.startTimes.length+" blocks");
        int blockNo = this.startTimes.length;
        //declare the pixel rows
        this.startPixels = new int[blockNo];
        this.endPixels = new int[blockNo];

        for(int i = 0; i<blockNo;i++)
        {
            LocalTime adjStart = startTimes[i].truncatedTo(ChronoUnit.MINUTES);
            LocalTime adjEnd = endTimes[i].truncatedTo(ChronoUnit.MINUTES);
            this.startTimes[i]=this.startTimes[i].truncatedTo(ChronoUnit.MINUTES);
            this.endTimes[i]=this.endTimes[i].truncatedTo(ChronoUnit.MINUTES);



            //lets get the minutes
            int h = adjStart.getHour();
            h = h * 60;
            h = h + adjStart.getMinute();
            //8:00 in minutes is 480;
            h = h - 480;

            int j = adjEnd.getHour();
            j = j * 60;
            j = j + adjEnd.getMinute();
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

            this.startPixels[i]=h;
            this.endPixels[i]=j;

        }
    }

    public void printDay()
    {
        for(int i = 0;i < this.startTimes.length;i++)
        {
            System.out.println("~~~~"+this.names[i]+"~~~~~");
            System.out.println(this.startTimes[i]);
            System.out.println(this.startPixels[i]);
            System.out.println(this.endTimes[i]);
            System.out.println(this.endPixels[i]);
            System.out.println("~~~~~~~~~~~~~~~");
        }
    }
}
