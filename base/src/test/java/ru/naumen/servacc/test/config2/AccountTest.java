package ru.naumen.servacc.test.config2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
import ru.naumen.servacc.config2.Account;

/**
 * @author Andrey Hitrin
 * @since 23.01.13
 */
public class AccountTest
{

    public final Account account = new Account();

    @Test
    public void defaultValues()
    {
        assertThat(account.getComment(), is(nullValue()));
        assertThat(account.getId(), is(nullValue()));
        assertThat(account.getLogin(), is(nullValue()));
        assertThat(account.getPassword(), is(nullValue()));
        assertThat(account.getType(), is(nullValue()));
        assertThat(account.getName(), is(nullValue()));
        assertThat(account.getThrough(), is(nullValue()));

        assertThat(account.getIconName(), is("/icons/card.png"));
        // is such string value ok?
        assertThat(account.toString(), is("(null)"));
    }

    @Test
    public void login()
    {
        account.setParams(new HashMap<String, String>() {{
            put(Account.ACCOUNT_PARAM_LOGIN, "pluto");
        }});
        assertThat(account.getLogin(), is("pluto"));
    }

    @Test
    public void password()
    {
        account.setParams(new HashMap<String, String>() {{
            put(Account.ACCOUNT_PARAM_PASSWORD, "secret");
        }});
        assertThat(account.getPassword(), is("secret"));
    }

    //
    // toString
    //

    @Test
    public void shouldContainType()
    {
        // TODO: type seems deprecated?
        account.setType("stub");
        assertThat(account.toString(), is("(stub)"));
    }

    @Test
    public void shouldAddAddressWhenItIsNotNull()
    {
        account.setType("citizen");
        account.setParams(new HashMap<String, String>() {{
            put(Account.ACCOUNT_PARAM_ADDRESS, "home");
        }});
        assertThat(account.toString(), is("(citizen)home"));
    }

    @Test
    public void shouldAddAddressAndLoginWhenBothAreNotNull()
    {
        account.setType("full");
        account.setParams(new HashMap<String, String>() {{
            put(Account.ACCOUNT_PARAM_ADDRESS, "hull");
            put(Account.ACCOUNT_PARAM_LOGIN, "cargo");
        }});
        assertThat(account.toString(), is("(full) cargo @ hull"));
    }

    @Test
    public void shouldNotAddLoginWhenAddressIsNull()
    {
        account.setType("orphan");
        account.setParams(new HashMap<String, String>() {{
            put(Account.ACCOUNT_PARAM_LOGIN, "Oliver");
        }});
        assertThat(account.toString(), is("(orphan)"));
    }

    //
    // matches
    //

    @Test
    public void mustNotMatchEmptyString()
    {
        assertThat(account.matches(null), is(false));
        assertThat(account.matches(""), is(false));
    }

    @Test
    public void mustMatchByNameOrCommentOrAddressOrLogin()
    {
        account.setParams(new HashMap<String, String>(){{
            put(Account.ACCOUNT_PARAM_LOGIN, "login");
            put(Account.ACCOUNT_PARAM_ADDRESS, "address");
            put(Account.ACCOUNT_PARAM_PASSWORD, "password");
        }});
        account.setName("name");
        account.setComment("comment");

        assertThat(account.matches("log"), is(true));
        assertThat(account.matches("ddr"), is(true));
        assertThat(account.matches("ame"), is(true));
        assertThat(account.matches("mm"), is(true));

        assertThat(account.matches("word"), is(false));
    }
}
