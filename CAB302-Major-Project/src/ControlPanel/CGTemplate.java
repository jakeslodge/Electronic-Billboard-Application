package ControlPanel;

import TestTools.ScheduleDay;

import javax.swing.*;
import java.awt.*;

/** Custom Drawing Code Template */
// A Swing application extends javax.swing.JFrame
class CGTemplate extends JFrame {
    // Define constants
    public static final int CANVAS_WIDTH  = 800;
    public static final int CANVAS_HEIGHT = 600;

    public ScheduleDay[] week;




    // Declare an instance of the drawing canvas,
    // which is an inner class called DrawCanvas extending javax.swing.JPanel.
    private DrawCanvas canvas;

    // Constructor to set up the GUI components and event handlers
    public CGTemplate(ScheduleDay[] week) {


        this.week = week;
        System.out.println(this.week[0]);
        canvas = new DrawCanvas();    // Construct the drawing canvas
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        // Set the Drawing JPanel as the JFrame's content-pane
        Container cp = getContentPane();
        cp.add(canvas);
     //   canvas.;
        // or "setContentPane(canvas);"

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // Handle the CLOSE button

        pack();              // Either pack() the components; or setSize()
        setTitle("......");  // "super" JFrame sets the title

        setVisible(true);    // "super" JFrame show
    }

    /**
     * Define inner class DrawCanvas, which is a JPanel used for custom drawing.
     */
    private class DrawCanvas extends JPanel {
        // Override paintComponent to perform your own painting
        @Override
        public void paintComponent(Graphics g) {
            int offset = 50;
            int boxOffset = 40;

            int verticalOffset=0;

            super.paintComponent(g);     // paint parent's background
            setBackground(Color.WHITE);  // set background color for this JPanel

            // Your custom painting codes. For example,
            // Drawing primitive shapes
            g.setColor(Color.BLACK);    // set the drawing color

            for(ScheduleDay day: week)
            {
                //lets take that day
                for(int z=0;z<day.startTimes.length;z++)
                {
                    g.drawRect(75+verticalOffset,day.startPixels[z]+boxOffset,100,day.endPixels[z]-day.startPixels[z]);
                    g.drawString(day.names[z],80+verticalOffset,day.startPixels[z]+boxOffset+20);
                    g.drawString(day.startTimes[z].toString(),80+verticalOffset,day.startPixels[z]+boxOffset+35);
                    g.drawString(day.endTimes[z].toString(),120+verticalOffset,day.startPixels[z]+boxOffset+35);
                    g.drawString(day.bookedBy[z],80+verticalOffset,day.startPixels[z]+boxOffset+50);
                    //date title
                    g.drawString(day.date.toString(),100+verticalOffset,15);
                }
                //increase the vertical offset
                verticalOffset = verticalOffset + 105;
            }



//            for (int i = 0;i<a.length;i++)
//            {
//                //add the offset as well
//
//                System.out.println("drawing from:"+a[i]+ " to "+b[i]);
//                //g.drawLine(75, a[i], 75, b[i]);
//                g.drawRect(75,a[i]+offset,120,b[i]-a[i]);
//                g.drawString(n[i],100,a[i]+offset+20);
//                g.drawString(c[i].toString(),100,a[i]+offset+35);
//                g.drawString(d[i].toString(),140,a[i]+offset+35);
//            }
//            //g.drawRect(75,270,200,420);



            // Printing texts
            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));


            g.drawString("8:00", 2, 0+offset);
            g.drawString("9:00", 2, 60+offset);
            g.drawString("10:00", 2, 120+offset);
            g.drawString("11:00", 2, 180+offset);
            g.drawString("12:00", 2, 240+offset);
            g.drawString("1:00", 2, 300+offset);
            g.drawString("2:00", 2, 360+offset);
            g.drawString("3:00", 2, 420+offset);
            g.drawString("4:00", 2, 480+offset);
            g.drawString("5:00", 2, 540+offset);

        }
    }


}