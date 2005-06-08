package echo2example.email;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextField;

/**
 * 
 */
public class PageNavigator extends Row {

    private Label totalLabel;
    private TextField pageField;
    
    public PageNavigator() {
        super();
        setCellSpacing(new Extent(20));
        
        Button previousPageButton = new Button(Styles.ICON_24_LEFT_ARROW);
        previousPageButton.setRolloverEnabled(true);
        previousPageButton.setRolloverIcon(Styles.ICON_24_LEFT_ARROW_ROLLOVER);
        add(previousPageButton);
        
        Row entryRow = new Row();
        entryRow.setCellSpacing(new Extent(5));
        add(entryRow);
        
        Label itemLabel = new Label(Messages.getString("PageNavigator.ItemLabel"));
        entryRow.add(itemLabel);
        
        pageField = new TextField();
        pageField.setWidth(new Extent(4, Extent.EX));
        pageField.setText("1");
        entryRow.add(pageField);
        
        Label prepositionLabel = new Label(Messages.getString("PageNavigator.PrepositionLabel"));
        entryRow.add(prepositionLabel);
        
        totalLabel = new Label("1");
        entryRow.add(totalLabel);
        
        Button nextPageButton = new Button(Styles.ICON_24_RIGHT_ARROW);
        nextPageButton.setRolloverEnabled(true);
        nextPageButton.setRolloverIcon(Styles.ICON_24_RIGHT_ARROW_ROLLOVER);
        add(nextPageButton);
    }
}
