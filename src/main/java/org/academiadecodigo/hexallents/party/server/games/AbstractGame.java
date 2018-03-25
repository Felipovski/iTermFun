package org.academiadecodigo.hexallents.party.server.games;

import org.academiadecodigo.hexallents.party.server.Score;
import org.academiadecodigo.hexallents.party.server.Server;

/**
 * Created by codecadet on 24/03/2018.
 */
public abstract class AbstractGame {

    protected Score score;
    protected Server server;
    protected int rounds;

    public AbstractGame(Score score, Server server, int rounds){
        this.score = score;
        this.server = server;
        this. rounds = rounds;
    }
}