import edu.smu.tspell.wordnet.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alessandro
 */
public class ConceptualSimilarity {

    static String pathToPairs = "[PATH_TO_INPUT_PAIRS_FILE]";
    static String pathToWNDict = "[PATH_TO_WN_DICT]";
    static String pathToOutputFile = "[PATH_TO_LOG_OUTPUT_FILE]";
    static WordNetDatabase database = WordNetDatabase.getFileInstance();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("wordnet.database.dir", pathToWNDict);
        File f = new File(pathToPairs);
        File log = new File(pathToOutputFile);
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log)));
            NounSynset nounSynset;
            String str = null;
            while ((str = bf.readLine()) != null) {
                if (!str.equals("")) {
                    String[] line = str.split("\\t");
                    
                    String firstTerm = line[0];
                    String secondTerm = line[1];
                    Synset[] synsetsFirstTerm = database.getSynsets(firstTerm, SynsetType.NOUN);
                    Synset[] synsetsSecondTerm = database.getSynsets(secondTerm, SynsetType.NOUN);
                    ArrayList<String> hypernymsNameListFirstTerm;
                    double conceptSimilarity = 0;
                    
                    for (int i = 0; i < synsetsFirstTerm.length; i++) {
                        nounSynset = (NounSynset) (synsetsFirstTerm[i]);
                        hypernymsNameListFirstTerm = getHypernymsList(nounSynset);
                        String firstSenseName = nounSynset.getWordForms()[0];
                        Sense firstSense = new Sense(firstSenseName, hypernymsNameListFirstTerm);
                        ArrayList<String> hypernymsNameListSecondTerm;
                        for (int j = 0; j < synsetsSecondTerm.length; j++) {
                            nounSynset = (NounSynset) (synsetsSecondTerm[j]);
                            hypernymsNameListSecondTerm = getHypernymsList(nounSynset);
                            String secondSenseName = nounSynset.getWordForms()[0];
                            Sense secondSense = new Sense(secondSenseName, hypernymsNameListSecondTerm);
                            double cs = computeConceptSimilarity(firstSense, secondSense);
                            if (cs > conceptSimilarity) {
                                conceptSimilarity = cs;
                            }
                        }
                    }
                    out.write("[" + firstTerm + "] e [" + secondTerm + "] : " + conceptSimilarity+"\n");
                }
            }
            bf.close();
            out.close();
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ioex) {
            System.err.println(ioex.getMessage());
        }
    }

    /**
     * Metodo usato per debugging. Stampa a video la lista degli iperonimi relativa ad un NounSynset passato in input
     * @param nounSynset synset da visualizzare
     * @param hypernyms lista di iperonimi
     */
    private void printHypernyms(NounSynset nounSynset, ArrayList<String> hypernyms){
        System.out.println("\nSense: " + nounSynset.getWordForms()[0]);
        for (String s : hypernyms) {
            System.out.println(s);
        }
        System.out.println("\n");
    }

    private static ArrayList<String> getHypernymNames(NounSynset nounSynset){
        ArrayList<String> hyp = new ArrayList<String>();
        String word = nounSynset.getWordForms()[0];
        hyp.add(word);
        return regGetHypernymNames(nounSynset, hyp);
    }
    
    private static ArrayList<String> regGetHypernymNames(NounSynset nounSynset, ArrayList<String> hyp){
        NounSynset[] hypernyms = nounSynset.getHypernyms();
        ArrayList<String> hypernymNames = new ArrayList<String>();
        for(NounSynset ns : hypernyms){
            String hypernym = ns.getWordForms()[0];
            System.err.println("REC_GET_HYP: "+ hypernym);
            hypernymNames.add(hypernym);
        }
        return null;
    }
    
    
    /**
     * Wrapper method that compute two terms' senses in order to calculate the
     * concept similarity between them,then find out and return the maximum
     * value.
     *
     * @param firstSense first term to compare
     * @param secondSense second term to compare
     */
    private static double computeConceptSimilarity(Sense firstSense, Sense secondSense) {
        int depthFirstSense = firstSense.getDepth();
        int depthSecondSense = secondSense.getDepth();
        int lcs = lowestCommonSubsumer(firstSense.getHypernymNames(), secondSense.getHypernymNames());
        return (2.0 * lcs) / ((depthFirstSense + depthSecondSense) * 1.0);
    }


    /**
     * Metodo wrapper che acquisisce la lista di iperonimi relativa al Synset passato in input
     * @param synset Synset del quale si vogliono ricavare gli iperonimi
     * @return lista degli iperonimi del synset
     */
    private static ArrayList<String> getHypernymsList(Synset synset) {
        ArrayList<String> hypernymsNameList = new ArrayList<String>();
        return recGetHypernymsList((NounSynset) synset, hypernymsNameList);
    }

    /**
     * Metodo ricorsivo di acquisizione degli iperonimi
     * @param nounSynset
     * @param hypernymsNameList
     * @return
     */
    private static ArrayList<String> recGetHypernymsList(NounSynset nounSynset, ArrayList<String> hypernymsNameList) {
        String word = nounSynset.getWordForms()[0];
        hypernymsNameList.add(word);
        NounSynset[] hypernyms = nounSynset.getHypernyms();
        if (hypernyms.length == 0) {
            return hypernymsNameList;
        }
        return recGetHypernymsList(hypernyms[0], hypernymsNameList);
    }

    /**
     * Calcola e restituisce il più basso antenato comune tra due liste di iperonimi passate in input.
     * @param hypFirstSense lista di iperonimi del primo senso
     * @param hypSecondSense lista di iperonimi del secondo senso
     * @return valore
     */
    private static int lowestCommonSubsumer(ArrayList<String> hypFirstSense, ArrayList<String> hypSecondSense) {
        int i;
        if ((hypFirstSense.size() == (hypSecondSense.size())) && hypFirstSense.get(0).equals(hypSecondSense.get(0))) {
            return hypFirstSense.size();
        } else if (hypFirstSense.size() >= hypSecondSense.size()) {
            for (i = 0; i < hypSecondSense.size(); i++) {
                String senseName = hypSecondSense.get(i);
                if (hypFirstSense.contains(senseName))
                    return (hypSecondSense.size() - (i + 1));
            }
        } else {
            for (i = 0; i < hypFirstSense.size(); i++) {
                String senseName = hypFirstSense.get(i);
                if (hypSecondSense.contains(senseName))
                    return (hypFirstSense.size() - (i + 1));
            }
        }
        return 0;
    }

    private static void printList(ArrayList<String> list){
        System.err.print("\n[");
        for(String s : list){
            System.err.print(s+" => ");
        }
        System.err.println("]");
    }
}

