/*
 * Copyright (C) 2012 Department of General and Computational Linguistics,
 * University of Tuebingen
 *
 * This file is part of the Java Relatedness API to GermaNet.
 *
 * The Java Relatedness API to GermaNet is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Java Relatedness API to GermaNet is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this API; if not, see <http://www.gnu.org/licenses/>.
 */
package de.tuebingen.uni.sfs.germanet.relatedness.gui;

/**
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.Synset;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class SynsetListRenderer extends JPanel implements ListCellRenderer {

    private JLabel image = new JLabel();
    private JLabel text = new JLabel("<html></html>");
    private final static Border NO_FOCUS_BORDER =
            new EmptyBorder(1, 1, 1, 1);

    SynsetListRenderer() {
        setLayout(new BorderLayout());
        add(image, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);

        text.setOpaque(false);
        image.setOpaque(false);
    }

    /**
     * 
     * @param list 
     * @param value the items of the DropBoxList
     * @param index
     * @param isSelected 
     * @param CellhasFocus
     * @return 
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean CellhasFocus) {
        //if the Object is a Synset, its' id and OrthForms will be shown in the list 
        if (value != null && value instanceof Synset) {
            Synset s = (Synset) value;
            String listVal = s.getId() + " " + s.getAllOrthForms(); //list item

            String tooltip = "<html>" + listVal + "<br>"; //constructing tooltip
            if (s.getParaphrase().length() > 0) {
                tooltip += "Paraphrase: " + s.getParaphrase() + "<br>";
            }

            tooltip += "Word Class: " + s.getWordClass().name() + "<br>Hypernym: "
                    + s.getRelatedSynsets(ConRel.has_hypernym).get(0).getAllOrthForms();
            tooltip += "</html>";

            if (listVal.length() > 43) { //if the list item is too long, crop
                listVal = listVal.substring(0, 40) + "...";
            }
            list.setToolTipText(tooltip);
            text.setText(listVal);
            //if the Object is a String ("Alle Synsets") the String will be shown
        } else if (value != null && value instanceof String) {
            text.setText(value.toString());
        } //else the EmptyString
        else {
            text.setText("");
        }
        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            background = Color.BLUE;
            foreground = Color.WHITE;

            // check if this cell is selected
        } else if (isSelected) {
            background = Color.BLUE;
            foreground = Color.WHITE;

            // unselected, and not the DnD drop location
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }

        setBackground(background);
        setForeground(foreground);

        return this;

    }
}
