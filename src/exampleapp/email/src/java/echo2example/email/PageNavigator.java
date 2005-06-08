package echo2example.email;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;

/**
 * 
 */
public class PageNavigator extends Row {

    public PageNavigator() {
        super();
        setCellSpacing(new Extent(20));
        
        Button previousPageButton = new Button(Messages.getString("PageNavigator.PreviousPage"));
        previousPageButton.setRolloverEnabled(true);
        previousPageButton.setIcon(Styles.ICON_24_LEFT_ARROW);
        previousPageButton.setRolloverIcon(Styles.ICON_24_LEFT_ARROW_ROLLOVER);
        add(previousPageButton);
        
        Button nextPageButton = new Button(Messages.getString("PageNavigator.NextPage"));
        nextPageButton.setRolloverEnabled(true);
        nextPageButton.setIcon(Styles.ICON_24_RIGHT_ARROW);
        nextPageButton.setRolloverIcon(Styles.ICON_24_RIGHT_ARROW_ROLLOVER);
        add(nextPageButton);
    }
}
