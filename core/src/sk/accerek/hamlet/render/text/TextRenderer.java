package sk.accerek.hamlet.render.text;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.NonNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.render.Anchor;
import sk.accerek.hamlet.render.text.font.FontStyle;
import sk.accerek.hamlet.render.text.font.IFont;

import java.util.*;
import java.util.stream.Collectors;

public class TextRenderer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private @NonNull final SpriteBatch spriteBatch;

    private final ArrayList<CachedFont> fontCache;
    private final ImmutableList<IFont> fonts;

    private final GlyphLayout glyphLayout;

    public TextRenderer(@NonNull SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;

        fontCache = new ArrayList<>();
        glyphLayout = new GlyphLayout();

        Set<Class<? extends IFont>> classes = new Reflections("sk.accerek.hamlet.render.text.font").getSubTypesOf(IFont.class);
        List<IFont> tempFonts = classes.stream()
                .map((clazz) -> {
                    try {
                        logger.debug("Loading font: " + clazz.getSimpleName());
                        return clazz.getConstructor().newInstance();
                    } catch (Exception e) {
                        logger.error("Failed to instantiate font!", e);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        fonts = ImmutableList.copyOf(tempFonts);

        logger.info("Initialised TextRenderer with " + fonts.size() + " fonts loaded!");
    }

    public void drawText(String font, int size, FontStyle style, Anchor horizontalAnchor, Anchor verticalAnchor, Color color, float x, float y, String text) {
        Optional<BitmapFont> bitmapFont = getFont(font, size, style);
        if(!bitmapFont.isPresent()) {
            throw new RuntimeException("Invalid font: " + font);
        }

        BitmapFont bitmapFont0 = bitmapFont.get();
        bitmapFont0.setColor(color);
        glyphLayout.setText(bitmapFont0, text);

        switch (horizontalAnchor) {
            case CENTRE: x += glyphLayout.width / 2;
            case RIGHT: x -= glyphLayout.width;
        }

        switch (verticalAnchor) {
            case CENTRE: y -= glyphLayout.height / 2;
            case BOTTOM: y += glyphLayout.height;
        }

        bitmapFont0.draw(spriteBatch, glyphLayout, x, y);
    }

    public void drawWrappedText(String font, int size, int width, FontStyle style, Anchor horizontalAnchor, Anchor verticalAnchor, int align, Color color, float x, float y, String text) {
        Optional<BitmapFont> bitmapFont = getFont(font, size, style);
        if(!bitmapFont.isPresent()) {
            throw new RuntimeException("Invalid font: " + font);
        }

        BitmapFont bitmapFont0 = bitmapFont.get();
        bitmapFont0.setColor(color);
        glyphLayout.setText(bitmapFont0, text, color, width, align, true);

        switch (horizontalAnchor) {
            case CENTRE:
                x -= glyphLayout.width / 2;
                break;
            case RIGHT:
                x += glyphLayout.width;
                break;
        }

        switch (verticalAnchor) {
            case CENTRE:
                y -= glyphLayout.height / 2;
                break;
            case BOTTOM:
                y += glyphLayout.height;
                break;
        }

        bitmapFont0.draw(spriteBatch, glyphLayout, x, y);
    }

    public Vector2 getPixelSize(String font, int size, boolean wrap, int width, FontStyle style, String text) {
        Optional<BitmapFont> bitmapFont = getFont(font, size, style);
        if(!bitmapFont.isPresent()) {
            throw new RuntimeException("Invalid font: " + font);
        }

        BitmapFont bitmapFont0 = bitmapFont.get();
        glyphLayout.setText(bitmapFont0, text, Color.WHITE, width, Align.left, true);

        return new Vector2(glyphLayout.width, glyphLayout.height);
    }

    private Optional<BitmapFont> getFont(String name, int size, FontStyle style) {
        if(!fontExists(name)) {
            return Optional.empty();
        }

        if(isFontCached(name, size, style)) {
            Optional<CachedFont> cachedFont = getCachedFont(name, size, style);
            return cachedFont.map(CachedFont::getFont);
        }

        Optional<IFont> font = fonts.stream().filter((f) -> f.getName().equals(name)).findFirst();
        if(!font.isPresent()) {
            return Optional.empty();
        }
        BitmapFont bitmapFont = font.get().generateFont(size, style);
        CachedFont cachedFont = new CachedFont(name, size, style);
        cachedFont.setFont(bitmapFont);
        fontCache.add(cachedFont);
        return Optional.of(bitmapFont);
    }

    private Optional<CachedFont> getCachedFont(String name, int size, FontStyle style) {
        CachedFont tempFont = new CachedFont(name, size, style);
        return fontCache.stream().filter((font) -> font.metaEquals(tempFont)).findFirst();
    }

    private boolean fontExists(String name) {
        return fonts.stream().anyMatch((font) -> font.getName().equals(name));
    }

    private boolean isFontCached(String name, int size, FontStyle style) {
        CachedFont tempFont = new CachedFont(name, size, style);
        return fontCache.stream().anyMatch((font) -> font.metaEquals(tempFont));
    }

    private @Data class CachedFont {
        private final String name;
        private final int size;
        private final FontStyle style;
        private BitmapFont font;

        boolean metaEquals(CachedFont other) {
            return name.equals(other.getName()) && size == other.getSize() && style == other.getStyle();
        }
    }
}
