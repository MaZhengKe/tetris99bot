package my.mk;

import lombok.AllArgsConstructor;
import lombok.Data;
import my.mk.ns.DPad;

@Data
@AllArgsConstructor
public class KeyDPad {
    private Boolean up;
    private Boolean down;
    private Boolean left;
    private Boolean right;

    public void up(){
        up = true;
        down = false;
    }
    public void down(){
        down = true;
        up = false;
    }
    public void left(){
        left = true;
        right = false;
    }
    public void right(){
        right =true;
        left = false;
    }
    public void releaseUp(){
        up = false;
    }
    public void releaseDown(){
        down = false;
    }
    public void releaseLeft(){
        left = false;
    }
    public void releaseRight(){
        right =false;
    }

    public DPad toDpad() {
        if (up && !down && !left && !right)
            return DPad.Up;
        if (!up && down && !left && !right)
            return DPad.Down;
        if (!up && !down && left && !right)
            return DPad.Left;
        if (!up && !down && !left && right)
            return DPad.Right;


        if (up && !down && left && !right)
            return DPad.UpLeft;
        if (up && !down && !left && right)
            return DPad.UpRight;

        if (!up && down && left && !right)
            return DPad.DownLeft;
        if (!up && down && !left && right)
            return DPad.DownRight;
        return DPad.None;
    }


}
