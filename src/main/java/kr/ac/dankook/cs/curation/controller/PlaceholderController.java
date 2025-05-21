package kr.ac.dankook.cs.curation.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/placeholder")
public class PlaceholderController {

    @GetMapping("/{width}/{height}")
    public ResponseEntity<byte[]> getPlaceholderImage(
            @PathVariable int width,
            @PathVariable int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 배경색 설정 (어두운 회색)
            g2d.setColor(new Color(34, 34, 34));
            g2d.fillRect(0, 0, width, height);
            
            // 텍스트 설정
            g2d.setColor(new Color(160, 160, 160));
            g2d.setFont(new Font("Arial", Font.PLAIN, Math.min(width, height) / 10));
            
            // 텍스트 중앙 정렬
            String text = "No Image";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            int x = (width - textWidth) / 2;
            int y = (height - textHeight) / 2 + fm.getAscent();
            
            g2d.drawString(text, x, y);
            g2d.dispose();
            
            // 이미지를 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 