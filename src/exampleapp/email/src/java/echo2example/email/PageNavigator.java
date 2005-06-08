package echo2example.email;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;

/**
 * 
 */
public class PageNavigator extends Row {

    public PageNavigator() {
        Button previousPageButton = new Button(Messages.getString("PageNavigator.PreviousPage"));
        add(previousPageButton);
        
        Button nextPageButton = new Button(Messages.getString("PageNavigator.NextPage"));
        add(nextPageButton);
    }
}
