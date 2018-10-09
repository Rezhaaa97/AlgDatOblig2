import java.util.ConcurrentModificationException;
import java.util.*;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DobbeltLenketListe<T> implements Liste<T> {

    private static final class Node<T> {
        private T verdi;
        private Node<T> forrige, neste;

        private Node(T verdi, Node<T> forrige, Node<T> neste) {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        protected Node(T verdi) {
            this(verdi, null, null);
        }

    }

    private Node<T> hode;
    private Node<T> hale;
    private int antall;
    private int endringer;

    private Node<T> finnNode(int indeks) {
        if(indeks < antall / 2) {
            Node<T> p = hode;
            for (int i = 0; i < indeks; i++) p = p.neste;
            return p;
        } else {
            Node<T> p = hale;
            for (int i = antall - 1; i > indeks; i--) p = p.forrige;
            return p;
        }
    }

    private static void fratilKontroll(int antall, int fra, int til) {
        if (fra < 0)
            throw new IndexOutOfBoundsException
                    ("fra(" + fra + ") er negativ !");
        if (til > antall)
            throw new IndexOutOfBoundsException
                    ("til(" + til + ") > antall(" + antall + ")");
        if (fra > til)
            throw new IllegalArgumentException
                    ("fra(" + fra + ") > til(" + til + ") - ulovlig intervall!");
    }
    public DobbeltLenketListe() {
        hode = hale = null;
        antall = 0;
        endringer = 0;
    }
    public DobbeltLenketListe(T[] a) {
        this();
        Objects.requireNonNull(a, "Tabellen a er null!");
        if (a.length == 0) return;
        Node<T> temp = hode;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null)
                continue;
            Node<T> newNode = new Node<>(a[i], null, null);
            if (antall == 0) {
                hode = hale = newNode;
            } else {
                newNode.forrige = temp;
                hale.neste = newNode;
                hale = newNode;
            }
            temp = newNode;
            antall++;
        }

    }
    public Liste<T> subliste(int fra, int til) {
        fratilKontroll(antall, fra, til);
        Liste<T> tmpListe = new DobbeltLenketListe<>();
        for (int i = fra; i < til; i++) {
            tmpListe.leggInn(finnNode(i).verdi);
        }
        return tmpListe;
    }
    @Override
    public int antall()
    {
        return antall;
    }
    @Override
    public boolean tom()
    {
        return antall == 0;
    }
    @Override
    public boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Null verdi");
        if (tom()) {
            hode = hale = new Node<>(verdi, null,null);
        } else {
            hale = hale.neste = new Node<>(verdi, hale, null);
        }
        antall++;
        endringer++;
        return true;
    }
    @Override
    public void leggInn(int indeks, T verdi) {
        Objects.requireNonNull(verdi, "Nullverdi er ikke tillatt");
        indeksKontroll(indeks, true);
        if (tom()) {
            hode = hale = new Node<>(verdi, null,null);
        } else if (indeks == 0) {
            hode = hode.forrige = new Node<>(verdi, null, hode);
        } else if (indeks == antall) {
            hale = hale.neste = new Node<>(verdi, hale, null);
        } else {
            Node<T> p = hode;
            for (int i = 1; i < indeks; i++) p = p.neste;
            p = new Node<>(verdi, p, p.neste);
            p.neste.forrige = p;
            p.forrige.neste = p;
        }
        antall++;
        endringer++;
    }
    @Override
    public boolean inneholder(T verdi) {
        return indeksTil(verdi) != -1;
    }
    @Override
    public T hent(int indeks) {
        indeksKontroll(indeks, false);
        return finnNode(indeks).verdi;
    }
    @Override
    public int indeksTil(T verdi) {
        if (verdi == null) return -1;
        Node<T> p = hode;

        for (int i = 0; i < antall; i++) {
            if (p.verdi.equals(verdi))
                return i;
            p = p.neste;
        }
        return -1;
    }
    @Override
    public T oppdater(int indeks, T nyverdi) {
        Objects.requireNonNull(nyverdi, "Kan ikke ha nullverdi !");
        indeksKontroll(indeks, false);
        Node<T> tmp = finnNode(indeks);
        T tmpVerdi = tmp.verdi;
        tmp.verdi = nyverdi;
        endringer++;
        return tmpVerdi;
    }
    @Override
    public boolean fjern(T verdi) {
        if (verdi == null) return false;
        Node<T> p = hode;
        while (p != null) {
            if (p.verdi.equals(verdi)) break;
            p = p.neste;
        }
        if (p == null) {
            return false;
        } else if (p == hode) {
            hode = hode.neste;
            if (antall > 1) hode.forrige = null;
        } else if (p == hale) {
            hale = hale.forrige;
            hale.neste = null;
        } else {
            p.forrige.neste = p.neste;
            p.neste.forrige = p.forrige;
        }
        p.verdi = null;
        endringer++;
        antall--;
        return true;
    }
    @Override
    public T fjern(int indeks) {
        indeksKontroll(indeks, false);
        T temp;
        if (indeks == 0) {
            temp = hode.verdi;
            if (antall > 1) {
                hode = hode.neste;
                hode.forrige = null;
            } else {
                hale = null;
                hode.neste = null;
            }
        } else if (indeks == antall - 1) {
            temp = hale.verdi;
            Node<T> tempHale = hale;
            hale = hale.forrige;
            tempHale.forrige = null;
            hale.neste = null;
        } else {
            Node<T> p = finnNode(indeks);
            temp = p.verdi;
            p.forrige.neste = p.neste;
            p.neste.forrige = p.forrige;
        }
        endringer++;
        antall--;
        return temp;
    }
    @Override
    public void nullstill() {
        Node<T> p = hode, q;
        while (p != null) {
            q = p.neste;
            p.neste = null;
            p.verdi = null;
            p.forrige = null;
            p = q;
        }
        hode = hale = null;
        endringer++;
        antall = 0;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append('[');
        if (!tom()) {
            Node<T> p = hode;
            s.append(p.verdi);
            p = p.neste;
            while (p != null) {
                s.append(',').append(' ').append(p.verdi);
                p = p.neste;
            }
        }
        s.append(']');
        return s.toString();
    }
    public String omvendtString() {
        if (tom()) return "[]";
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        Node<T> p = hale;
        while (p != null){
            joiner.add(String.valueOf(p.verdi));
            p = p.forrige;
        }
        return joiner.toString();
    }

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {
        for (int n = liste.antall(); n > 1; n--) {
            for (int i = 1; i < n; i++) {
                T verdiA = liste.hent(i-1);
                T verdiB = liste.hent(i);
                if(c.compare(verdiA, verdiB) > 0) {
                    liste.oppdater(i-1, verdiB);
                    liste.oppdater(i, verdiA);
                }
            }
        }
    }
    @Override
    public Iterator<T> iterator() {
        return new DobbeltLenketListeIterator();
    }
    public Iterator<T> iterator(int indeks) {
        indeksKontroll(indeks, false);
        return new DobbeltLenketListeIterator(indeks);
    }
    private class DobbeltLenketListeIterator implements Iterator<T> {
        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;
        private DobbeltLenketListeIterator() {
            denne = hode;
            fjernOK = false;
            iteratorendringer = endringer;
        }
        private DobbeltLenketListeIterator(int indeks) {
            this();
            denne = finnNode(indeks);
        }
        @Override
        public boolean hasNext()
        {
            return denne != null;
        }
        @Override
        public T next() {
            if (iteratorendringer != endringer) {
                throw new ConcurrentModificationException("iteratorendringer samsvarer ikke med endringer");
            }
            else if (!hasNext()){
                throw new NoSuchElementException("Ikke flere elementer i lista!");
            }
            fjernOK = true;
            T tmp = denne.verdi;
            denne = denne.neste;
            return tmp;
        }
        @Override
        public void remove() {
            if (!fjernOK) throw new IllegalStateException("Ikke tillatt å kalle metoden / fjernOK == false");
            if (endringer != iteratorendringer)
                throw new ConcurrentModificationException("Listen er endret / endringer != iteratorendringer");
            fjernOK = false;
            if (antall == 1) {
                hale = null;
                hode.neste = null;
            } else if (denne == null) {
                Node<T> tempHale = hale;
                hale = hale.forrige;
                hale.neste = null;
                tempHale.forrige = null;
            } else if (denne.forrige == hode) {
                hode = hode.neste;
                hode.forrige = null;
            } else {
                Node<T> temp = denne.forrige.forrige;
                temp.neste = denne;
                denne.forrige = temp;
            }
            endringer++;
            iteratorendringer++;
            antall--;
        }
    } // DobbeltLenketListeIterator
} // DobbeltLenketListe
