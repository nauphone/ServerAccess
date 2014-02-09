package ru.naumen.servacc.test.config2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static ru.naumen.servacc.test.config2.ConfigStub.config;
import static ru.naumen.servacc.test.config2.ConfigStub.group;
import static ru.naumen.servacc.test.config2.ConfigStub.httpAccount;

import org.junit.Test;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.Path;
import ru.naumen.servacc.test.config2.SSHAccountStub;

/**
 * @author Andrey Hitrin
 * @since 02.03.13
 */
public class PathTest
{
    @Test
    public void whenAccountIsNotFoundThenItReturnsNothing()
    {
        Path path = Path.find(config(new SSHAccountStub("user@prntagon", "user@pentagon")), "ssh://bill@microsoft.com");
        assertThat(path.found(), is(false));
        assertThat(path.path(), is(""));
        assertThat(path.account(), is(nullValue()));
    }

    @Test
    public void whenAccountIsFoundThenItIsReturned()
    {
        SSHAccount notMatching = new SSHAccountStub("user@somewhere", "hello");
        SSHAccount matching = new SSHAccountStub("ssh://root@example.com", "blah");
        Path path = Path.find(config(group("", notMatching), httpAccount(), matching), "ssh://root@example.com");
        assertThat(path.found(), is(true));
        assertThat(path.path(), is(" > blah"));
        assertThat(path.account(), is(matching));
    }

    @Test
    public void onlyTheFirstAccountWithGivenIdentityIsUsed()
    {
        SSHAccount used = new SSHAccountStub("ssh://root@localhost", "win");
        SSHAccount notUsedBecauseIsSecond = new SSHAccountStub( "ssh://root@localhost", "fail" );
        Path path = Path.find(config(used, notUsedBecauseIsSecond), "ssh://root@localhost");
        assertThat(path.found(), is(true));
        assertThat(path.path(), is(" > win"));
        assertThat(path.account(), is(used));
    }

    @Test
    public void eachGroupAddsOneLevelOfDescription()
    {
        SSHAccount account = new SSHAccountStub("me@there", "account");
        Path path = Path.find(config(group("root group",
            group("subgroup",
                account))), "me@there");
        assertThat(path.found(), is(true));
        assertThat(path.path(), is("root group > subgroup > account"));
        assertThat(path.account(), is(account));
    }

    @Test
    public void httpAccountCannotBeFound()
    {
        Path path = Path.find(config(httpAccount()), "me@there");
        assertThat(path.found(), is(false));
        assertThat(path.path(), is(""));
        assertThat(path.account(), is(nullValue()));
    }
}
