package sk.accerek.hamlet.render.text.font;

public class PixelFont implements IFont {
    @Override
    public String getName() {
        return "pixel";
    }

    @Override
    public boolean supportsStyle(FontStyle style) {
        return style == FontStyle.REGULAR;
    }
}
