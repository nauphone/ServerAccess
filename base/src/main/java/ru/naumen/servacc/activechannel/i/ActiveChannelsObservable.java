/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.activechannel.i;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public interface ActiveChannelsObservable
{
    void setObserver(ActiveChannelsObserver observer);

    void notifyObserver();
}
