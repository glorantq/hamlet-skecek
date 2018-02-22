package sk.accerek.hamlet.render.text.font;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public interface IFont {
    String getName();
    boolean supportsStyle(FontStyle style);

    default BitmapFont generateFont(int size, FontStyle style) {
        if(!supportsStyle(style)) {
            throw new RuntimeException("This font doesn't support the " + style.name() + " style!");
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + getName() + "-" + style.name().toLowerCase() +  ".ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = Color.WHITE;
        parameter.characters = "0123456789öüóÄßQWERTZUIOPŐÚASDFGHJKLÉÁŰÍYXCVBNMŐÚŰÁÉäqwertzuiopőúasdfghjkléáűíyxcvbnűámúő,.-?:_<>#&@{};*~^°§'\"\\+!%/=()€$";
        parameter.genMipMaps = true;

        return generator.generateFont(parameter);
    }
}
