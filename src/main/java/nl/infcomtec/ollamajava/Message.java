/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.ollamajava;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * One message.
 */
public class Message {

    /**
     * Role must be "system", "user", "assistant" or "tool".
     */
    public static enum Roles {
        system, user, assistant, tool
    }
    /**
     * Role must be "system", "user", "assistant" or "tool".
     */
    public String role;
    /**
     * The content of the message, just a String.
     */
    public String content;
    /**
     * An array of base64-encoded images, for use with multimodal models
     * such as llava.
     */
    public String[] images;

    public Message(String role, String content) {
        this(Roles.valueOf(role), content);
    }

    public Message(Roles role, String content) {
        this.role = role.name();
        this.content = content;
    }

    public Message(String role, String content, RenderedImage[] images) throws Exception {
        this(Roles.valueOf(role), content, images);
    }

    public Message(Roles role, String content, RenderedImage... images) throws Exception {
        this.role = role.name();
        this.content = content;
        if (null != images) {
            for (RenderedImage im : images) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(im, "png", baos);
                    baos.flush();
                    String enc = Base64.getEncoder().encodeToString(baos.toByteArray());
                    if (null == this.images) {
                        this.images = new String[1];
                        this.images[0] = enc;
                    } else {
                        String[] oi = this.images;
                        this.images = new String[oi.length + 1];
                        System.arraycopy(oi, 0, this.images, 0, oi.length);
                        this.images[oi.length] = enc;
                    }
                }
            }
        }
    }

}
