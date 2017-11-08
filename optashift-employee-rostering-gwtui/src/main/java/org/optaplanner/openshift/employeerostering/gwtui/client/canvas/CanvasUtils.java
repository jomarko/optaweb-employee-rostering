package org.optaplanner.openshift.employeerostering.gwtui.client.canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D.FillStyleUnionType;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;

public class CanvasUtils {
    public static String FONT_FAMILY = "Arial";
    
    public static void drawLine(CanvasRenderingContext2D g, double x1, double y1, double x2, double y2) {
        g.beginPath();
        g.moveTo(x1, y1);
        g.lineTo(x2, y2);
        g.closePath();
        g.stroke();
    }
    
    public static String getFont(int size) {
        return size + "px " + FONT_FAMILY;
    }
    
    public static int fitTextToBox(CanvasRenderingContext2D g, String text, double w, double h) {
        String oldFont = g.font;
        String oldBaseline = g.textBaseline;
        
        int size = 1;
        double m;
        
        g.textBaseline = TextBaseline.TOP.getValue();
        do {
            size++;
            g.font = getFont(size);
            m = g.measureText(text).width;
        }
        while (m < w);
        size--;
        
        while (size > 1 && getTextHeight(g, size) > h) {
            size--;
        }
        
        g.font = oldFont;
        g.textBaseline = oldBaseline;
        return size;
    }
    
    public static double getTextHeight(CanvasRenderingContext2D g, int size) {
        String oldFont = g.font;
        g.font = getFont(size);
        double out = g.measureText("M").width;
        g.font = oldFont;
        return out;
    }
    
    public static void drawTextInBox(CanvasRenderingContext2D g, String text, double x, double y, double width, double height) {
        String[] words = text.split("\\s");
        if (words.length < 1) {
            return;
        }
        
        String oldFont = g.font;
        int textSize = 32;
        while (getTextHeight(g, textSize) > height) {
            textSize--;
        }
        
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
       
        textSize++;
        do {
            textSize--;
            g.font = getFont(textSize);
            
            List<String> splitWords = splitWord(g, words[0], width);
            lines.addAll(splitWords.subList(0, splitWords.size() - 1));
            line.append(splitWords.get(splitWords.size() - 1));
            
            for (int i = 1; i < words.length; i++) {
                if (g.measureText(line.toString() + " " + words[i]).width < width) {
                    line.append(' ').append(words[i]);
                }
                else {
                    lines.add(line.toString());
                    line.setLength(0);
                    if (getTextHeight(g, textSize)*lines.size() > height) {
                        lines.clear();
                        splitWords = splitWord(g, words[0], width);
                        lines.addAll(splitWords.subList(0, splitWords.size() - 1));
                        line.append(splitWords.get(splitWords.size() - 1));
                        textSize--;
                        g.font = getFont(textSize);
                        i = 0;
                        continue;
                    }
                    else {
                        splitWords = splitWord(g, words[i], width);
                        lines.addAll(splitWords.subList(0, splitWords.size() - 1));
                        line.append(splitWords.get(splitWords.size() - 1));
                    }
                }
            }
            lines.add(line.toString());
            line.setLength(0);
        }
        while(getTextHeight(g, textSize)*lines.size() > height);
        
        g.font = getFont(textSize);
        double lineHeight = getTextHeight(g, textSize);
        for (int i = 0; i < lines.size(); i++) {
            g.fillText(lines.get(i), x, y + lineHeight*(i + 1));
        }
        g.font = oldFont;
    }
    
    public static double[] getPreferredBoxSizeForText(CanvasRenderingContext2D g, String text, int textSize) {
        double maxWidth = 0;
        double lineHeight = getTextHeight(g, textSize);
        String[] lines = text.split("\r?\n");
        
        for (String line : lines) {
            double width = g.measureText(line).width;
            maxWidth = Math.max(width, maxWidth);
        }
        return new double[] {maxWidth, (1 + lines.length) * lineHeight};
    }
    
    private static List<String> splitWord(CanvasRenderingContext2D g, String word, double width) {
        if (g.measureText(word).width < width) {
            return Arrays.asList(word);
        }
        String remaining = word;
        ArrayList<String> out = new ArrayList<>();
        do {
            StringBuilder curr = new StringBuilder();
            curr.append(remaining.charAt(0));
            int i = 1;
            while (g.measureText(curr.toString()).width < width && i < remaining.length()) {
                curr.append(remaining.charAt(i));
            }
            out.add(curr.toString());
            remaining = remaining.substring(i);
        }
        while(!remaining.isEmpty());
        return out;
    }
    
    public static void drawCurvedRect(CanvasRenderingContext2D g, double x, double y, double width, double height) {
        double radius = Math.min(width, height)/3;
        g.beginPath();
        g.moveTo(x + radius, y);
        g.lineTo(x + width - radius, y);
        g.quadraticCurveTo(x + width, y, x + width, y + radius);
        g.lineTo(x + width, y + height - radius);
        g.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
        g.lineTo(x + radius, y + height);
        g.quadraticCurveTo(x, y + height, x, y + height - radius);
        g.lineTo(x, y + radius);
        g.quadraticCurveTo(x, y, x + radius, y);
        g.closePath();
        g.fill();
    }
    
    public static void setFillColor(CanvasRenderingContext2D g, String color) {
        g.fillStyle = FillStyleUnionType.of(color);
    }
    
    public static double getCanvasX(HTMLCanvasElement canvas, MouseEvent e) {
        return e.pageX - canvas.offsetLeft;
    }
    
    public static double getCanvasY(HTMLCanvasElement canvas, MouseEvent e) {
        return e.pageY - canvas.offsetTop;
    }
}