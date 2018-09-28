/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc;

/**
 * @author vtarasov
 * @since 15.03.16
 */
public interface IAuthenticationParametersGetter
{
    void setResourcePath(String resourcePath);
    
    void doGet();
    
    String getLogin();
    String getPassword();
}
