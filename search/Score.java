package search;

import engine.SongDocument;

public class Score implements Comparable <Score>
{
    public SongDocument _document;
    public double _score;

    public Score(SongDocument document, double score)
    {
        this._document = document;
        this._score = score;
    }

    @Override
    public int compareTo(Score o)
    {
        if (this._score > o._score) return -1;
        else if (this._score < o._score) return 1;
        return 0;
    }
}
