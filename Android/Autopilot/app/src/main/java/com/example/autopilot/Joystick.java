package com.example.autopilot;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Joystick {
    private int outerCircleCenterPositionY, outerCircleCenterPositionX,innerCircleCenterPositionX,innerCircleCenterPositionY, outerCircleRadius, innerCircleRadius;
    private Paint innerCirclePaint,outerCirclePaint;
    private double joystickCenterToTouchDistance;
    private boolean isPressed;
    public static double actuatorX, actuatorY;

    public Joystick(int centerPositionX, int centerPositionY, int innerCircleRadius, int outerCircleRadius)
    {
        this.outerCircleCenterPositionX = centerPositionX;
        this.outerCircleCenterPositionY = centerPositionY;
        this.innerCircleCenterPositionX = centerPositionX;
        this.innerCircleCenterPositionY = centerPositionY;

        this.outerCircleRadius = outerCircleRadius;
        this.innerCircleRadius = innerCircleRadius;
        this.outerCirclePaint = new Paint();
        this.outerCirclePaint.setColor(Color.GRAY);
        this.outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        this.innerCirclePaint = new Paint();
        this.innerCirclePaint.setColor(Color.BLUE);
        this.innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }
    public void draw(Canvas canvas)
    {
        canvas.drawCircle(this.outerCircleCenterPositionX,this.outerCircleCenterPositionY,this.outerCircleRadius,this.outerCirclePaint);
        canvas.drawCircle(this.innerCircleCenterPositionX,this.innerCircleCenterPositionY,this.innerCircleRadius,this.innerCirclePaint);


    }
    public boolean isPressed(double touchPositionX, double touchPositionY)
    {
        this.joystickCenterToTouchDistance = Math.sqrt(
                Math.pow(outerCircleCenterPositionX - touchPositionX,2) +
                        Math.pow(outerCircleCenterPositionY - touchPositionY,2)
        );
        return joystickCenterToTouchDistance < outerCircleRadius;
    }
    public void setIsPressed(boolean isPressed)
    {
        this.isPressed = isPressed;
    }
    public boolean getIsPressed()
    {
        return this.isPressed;
    }
    public void setActuator(double touchPositionX, double touchPositionY)
    {
        double deltaX = touchPositionX - this.outerCircleCenterPositionX;
        double deltaY = touchPositionY - outerCircleCenterPositionY;
        double deltaDistance = Math.sqrt(Math.pow(deltaX,2)+ Math.pow(deltaY,2));
        if(deltaDistance < this.outerCircleRadius)
        {
            this.actuatorX = deltaX / this.outerCircleRadius;
            this.actuatorY = deltaY / this.outerCircleRadius;
        } else {
            this.actuatorX = deltaX / deltaDistance;
            this.actuatorY = deltaY / deltaDistance;
        }
    }
    public void resetActuator()
    {
        this.actuatorX= 0.0;
        this.actuatorY = 0.0;
    }
    public void update()
    {
        this.innerCircleCenterPositionX = (int) (this.outerCircleCenterPositionX + this.actuatorX*this.outerCircleRadius);
        this.innerCircleCenterPositionY = (int) (this.outerCircleCenterPositionY + this.actuatorY*this.outerCircleRadius);
    }
}
