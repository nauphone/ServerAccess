/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc;

import com.mindbright.ssh2.SSH2Interactor;
import com.mindbright.ssh2.SSH2UserCancelException;

public class SSH2PasswordInteractor implements SSH2Interactor
{
    private String password;

    public SSH2PasswordInteractor(String password)
    {
        this.password = password;
    }

    public String promptLine(String prompt, boolean echo)
        throws SSH2UserCancelException
    {
        return null;
    }

    public String[] promptMulti(String[] prompts, boolean[] echos)
        throws SSH2UserCancelException
    {
        return null;
    }

    public String[] promptMultiFull(String name, String instruction,
                                    String[] prompts, boolean[] echos)
        throws SSH2UserCancelException
    {
        if (prompts.length == 0)
        {
            return new String[] {};
        }
        else if (prompts.length == 1 && prompts[0].startsWith("Password"))
        {
            return new String[] { password };
        }
        else
        {
            throw new SSH2UserCancelException(
                "Unknown prompt for keyboard interaction");
        }
    }

    public int promptList(String name, String instruction, String[] choices)
        throws SSH2UserCancelException
    {
        return 0;
    }
}
