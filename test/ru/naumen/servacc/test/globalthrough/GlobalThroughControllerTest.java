package ru.naumen.servacc.test.globalthrough;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ru.naumen.servacc.SSH2Backend;
import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.globalthrough.GlobalThroughController;
import ru.naumen.servacc.globalthrough.GlobalThroughView;

/**
 * @author Andrey Hitrin
 * @since 10.01.13
 */
public class GlobalThroughControllerTest
{
    private GlobalThroughViewStub view = new GlobalThroughViewStub();

    private SSH2BackendStub backend = new SSH2BackendStub();

    private GlobalThroughController controller = new GlobalThroughController(view, backend);

    //
    // clear()
    //

    @Test
    public void globalWidgetIsCleared()
    {
        view.cleared = false;
        controller.clear();
        assertThat(view.cleared, is(true));
    }

    @Test
    public void backendIsCleared()
    {
        backend.cleared = false;
        controller.clear();
        assertThat(backend.cleared, is(true));
    }

    //
    // select()
    //

    @Test
    public void whenAccountIsFoundThenItIsSetAsGlobalThrough()
    {
        SSHAccount notMatching = new SSHAccountStub("user@somewhere", "hello");
        SSHAccount matching = new SSHAccountStub("ssh://root@example.com", "blah");
        controller.select("ssh://root@example.com", config(group("", notMatching), httpAccount(), matching));
        assertThat(view.text, is(" > blah"));
        assertThat(backend.global, is(matching));
    }

    @Test
    public void onlyTheFirstAccountWithGivenIdentityIsUsed()
    {
        SSHAccount used = new SSHAccountStub("ssh://root@localhost", "win");
        SSHAccount notUsedBecauseIsSecond = new SSHAccountStub( "ssh://root@localhost", "fail" );
        controller.select("ssh://root@localhost", config(used, notUsedBecauseIsSecond));
        assertThat(view.text, is(" > win"));
        assertThat(backend.global, is(used));
    }

    @Test
    public void eachGroupAddsOneLevelOfDescription()
    {
        SSHAccount account = new SSHAccountStub("me@there", "account");
        controller.select("me@there", config(group("root group",
                                                group("subgroup",
                                                    account))));
        assertThat(view.text, is("root group > subgroup > account"));
        assertThat(backend.global, is(account));
    }

    private IConfigItem group(String name, IConfigItem... items)
    {
        Group root = new Group(name, "");
        for (IConfigItem item : items)
        {
            root.getChildren().add(item);
        }
        return root;
    }

    @Test
    public void httpAccountCannotBeUsedAsGlobalThrough()
    {
        controller.select("me@there", config(httpAccount()));
        assertThat(view.text, is(nullValue()));
        assertThat(backend.global, is(nullValue()));
    }

    //
    // refresh
    //

    @Test
    public void whenNoGlobalThroughAccountIsSetThenRefreshCleansItAgain()
    {
        view.cleared = false;
        backend.cleared = false;
        controller.refresh(config(new SSHAccountStub("hey@there", "hey")));
        assertThat(view.cleared, is(true));
        assertThat(backend.cleared, is(true));
    }

    @Test
    public void existingAccountIsBeingReselectOnRefresh()
    {
        SSHAccount account = new SSHAccountStub("tom@jerry", "tom");
        controller.select("tom@jerry", config(account));
        controller.refresh(config(group("home", account)));
        assertThat(view.text, is("home > tom"));
        assertThat(backend.global, is(account));
    }

    @Test
    public void whenGlobalThroughAccountIsNotFoundAfterRefreshThenItShouldBeCleared()
    {
        SSHAccount account = new SSHAccountStub("user@host", "user@host");
        controller.select("user@host", config(account));
        controller.refresh(config(new SSHAccountStub("admin@host", "admin@host")));
        assertThat(view.cleared, is(true));
        assertThat(backend.cleared, is(true));
    }

    public static IConfigItem httpAccount()
    {
        return new HTTPAccount();
    }

    public static IConfig config(IConfigItem... items)
    {
        return new ConfigStub(Arrays.asList(items));
    }

    private static class GlobalThroughViewStub implements GlobalThroughView
    {
        public boolean cleared;
        public String text;

        @Override
        public void setGlobalThroughWidget(String globalThroughText)
        {
            text = globalThroughText;
        }

        @Override
        public void clearGlobalThroughWidget()
        {
            cleared = true;
        }
    }

    private static class SSH2BackendStub extends SSH2Backend
    {
        public boolean cleared;
        public SSHAccount global;

        @Override
        public void setGlobalThrough(SSHAccount account)
        {
            global = account;
            cleared = (account == null);
        }
    }

    private static class ConfigStub implements IConfig
    {
        private List<IConfigItem> children;

        private ConfigStub(List<IConfigItem> children)
        {
            this.children = children;
        }

        @Override
        public List<IConfigItem> getChildren()
        {
            return children;
        }
    }

    private static class SSHAccountStub extends SSHAccount
    {
        private final String identity;
        public final String stringId;

        public SSHAccountStub(String identity, String stringId)
        {
            this.identity = identity;
            this.stringId = stringId;
        }

        @Override
        public String getUniqueIdentity()
        {
            return identity;
        }

        @Override
        public String toString()
        {
            return stringId;
        }
    }
}
