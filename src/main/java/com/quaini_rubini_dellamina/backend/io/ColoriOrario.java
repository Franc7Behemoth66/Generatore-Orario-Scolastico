
package com.quaini_rubini_dellamina.backend.io;

import java.awt.Color;
import java.util.Map;
import java.util.List;
import java.util.TreeSet;
import java.util.HashMap;

import com.quaini_rubini_dellamina.backend.entità.Slot;

public class ColoriOrario {

    public static final Color[] COLORI = {
            new Color(255, 179, 186),
            new Color(255, 223, 186),
            new Color(255, 255, 186),
            new Color(186, 255, 201),
            new Color(186, 225, 255),
            new Color(218, 186, 255),
            new Color(255, 204, 229),
            new Color(204, 255, 242),
            new Color(230, 230, 250),
            new Color(240, 230, 140),
            new Color(255, 218, 193),
            new Color(202, 255, 191),
            new Color(189, 224, 254),
            new Color(255, 198, 255),
            new Color(253, 255, 182),
            new Color(160, 196, 255),
            new Color(255, 214, 165),
            new Color(155, 246, 255),
            new Color(224, 187, 228),
            new Color(210, 235, 190)
    };

    public static Map<String, Color> buildColoriMaterie(List<Slot> slots) {
        TreeSet<String> materie = new TreeSet<>();

        for (Slot slot : slots) {
            if (slot.isAssegnato())
                materie.add(String.valueOf(slot.getLezione().getMateria().getNome()));
        }

        Map<String, Color> mappa = new HashMap<>();

        int i = 0;
        for (String materia : materie) {
            mappa.put(materia, COLORI[i % COLORI.length]);
            i++;
        }
        return mappa;
    }

    public static Color getColoreProfessore() {
        return COLORI[5];
    }
}
