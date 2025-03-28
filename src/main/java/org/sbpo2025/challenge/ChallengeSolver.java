package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ChallengeSolver {
    private final long MAX_RUNTIME = 600000; // milliseconds; 10 minutes

    protected List<Map<Integer, Integer>> orders;
    protected List<Map<Integer, Integer>> aisles;
    protected int nItems;
    protected int waveSizeLB;
    protected int waveSizeUB;

    protected Set<Integer> selectedOrders_ = new HashSet<>();
    protected Set<Integer> selectedAisles_ = new HashSet<>(); 
    protected Set<Integer> outAisles_ = new HashSet<>(); 
    protected int itensAttended_ = 0;

    public ChallengeSolver(
            List<Map<Integer, Integer>> orders, List<Map<Integer, Integer>> aisles, int nItems, int waveSizeLB, int waveSizeUB) {
        this.orders = orders;
        this.aisles = aisles;
        this.nItems = nItems;
        this.waveSizeLB = waveSizeLB;
        this.waveSizeUB = waveSizeUB;
    }

    public ChallengeSolution solve(StopWatch stopWatch) {
        
        System.out.println("Pedidos:");
        System.out.println(orders.size());

        int[] NItensOrder = new int[orders.size()];

        for (int j = 0; j < orders.size(); j++) {
            int cnt = 0;
            for (Map.Entry<Integer, Integer> entry : orders.get(j).entrySet()) {
                
                int value = entry.getValue();
                cnt += value;
            }

            NItensOrder[j] = cnt;
        }

        int cnt = 0;
        while(itensAttended_<= waveSizeUB){

            if(getRemainingTime(stopWatch) <= 30) break;

            if(cnt == 0) System.out.println("\n\nItens coletados: "+ itensAttended_ + " de " + waveSizeUB);

            Map.Entry<Integer, Integer> choice = choiceAisleOrder();
            int MxAisle = choice.getKey();
            int MxOrder = choice.getValue();

            if(MxAisle == -1 || MxOrder == -1){
                break;
            } 

            int OneMore = update(MxAisle, MxOrder);

            if(OneMore == 1 && NItensOrder[MxOrder] <= (waveSizeUB-itensAttended_)){
              //  System.out.println("Pedido Adicionado a solução " + MxOrder);
                selectedOrders_.add(MxOrder);
                itensAttended_ += NItensOrder[MxOrder];
            }

            if(cnt == 0) System.out.println("#### Tempo: " + getRemainingTime(stopWatch) + "s ###########");

            cnt += 1;
            cnt = (cnt%100);
        }

        System.out.println("Pedidos Atendidos: " + selectedOrders_.size()+ " Itens Entregues: " + itensAttended_  + " Corredores usados: :" + selectedAisles_.size());        
        System.out.println("#### Tempo: " + getRemainingTime(stopWatch) + "s ###########");
        return new ChallengeSolution(selectedOrders_, selectedAisles_);
    }

    private Map.Entry<Integer, Integer> choiceAisleOrder(){
        int MxAttended = 0; int MxRate = 0;int MxAisle = -1; int MxOrder = -1;
        int MxAttendedN = 0; int MxRateN = 0;int MxAisleN = -1; int MxOrderN = -1;
        
        // Select an aisle and an order by max rate number of get itens
        for (int i = 0; i < aisles.size(); i++) {
            if(outAisles_.contains(i)) continue;
            //System.out.println("## Corredor " + i + "##");
            for (int j = 0; j < orders.size(); j++) {
                
                if(selectedOrders_.contains(j)) continue;
                
                //System.out.println("\nPedido "+ j + " " + orders.get(j).size());
                
                int SmItens = 0;
                int AttendedItens=0;
                for (Map.Entry<Integer, Integer> entry : orders.get(j).entrySet()) {
                    int key = entry.getKey();
                    int value = entry.getValue();
                                        
                    //System.out.println("Item: " + key + ", Qtd: " + value);
                    
                    if(aisles.get(i).get(key) != null){
                        int has = aisles.get(i).get(key);
                        AttendedItens += Math.min(value,has);
                    }
                    
                    SmItens += value;
                }

                if(AttendedItens > (waveSizeUB-itensAttended_) || SmItens == 0 || AttendedItens == 0) continue;  

                int rate = (AttendedItens*100)/SmItens;
                
                if(selectedAisles_.contains(i)){
                    if(rate > MxRateN){
                        MxAisleN = i; MxOrderN = j; MxAttendedN = AttendedItens; MxRateN = rate;
                    }else if(rate == MxRate && AttendedItens > MxAttended){
                        MxAisleN = i; MxOrderN = j; MxAttendedN = AttendedItens; MxRateN = rate;
                    }
                }else{
                    if(rate > MxRate ){
                        MxAisle = i; MxOrder = j; MxAttended = AttendedItens; MxRate = rate;
                    }else if(rate == MxRate && AttendedItens > MxAttended){
                        MxAisle = i; MxOrder = j; MxAttended = AttendedItens; MxRate = rate;
                    }
                }
            }
        }

        if(MxAttendedN != 0){
            if(MxAisleN != -1 && !selectedAisles_.contains(MxAisleN)){
                selectedAisles_.add(MxAisleN);
               // System.out.println("Corredor Adicionado a solução " + MxAisleN);
            }
            
            //System.out.println("Escolhidos. Corredor:" + MxAisleN + " Pedido: " + MxOrderN + " Itens Atendidos: " + MxAttendedN + " " + MxRateN);

            return new AbstractMap.SimpleEntry<>(MxAisleN, MxOrderN);
        }else{
                if(MxAttended == 0){
                    MxAisle = -1; MxOrder = -1;
                }

                if(MxAisle != -1 && !selectedAisles_.contains(MxAisle)){
                    selectedAisles_.add(MxAisle);
//                    System.out.println("Corredor Adicionado a solução " + MxAisle);
                }
        
                return new AbstractMap.SimpleEntry<>(MxAisle, MxOrder);
        }

    }

    private int update(int aisle, int order){
        int left_itens = 0;
        
        for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            
            
            //System.out.println("Item: " + key + ", Qtd: " + value);
            if(aisles.get(aisle).get(key) != null){
                int has = aisles.get(aisle).get(key);
                int used = Math.min(value,has);

                aisles.get(aisle).put(key, has-used);
                orders.get(order).put(key, value-used);
                left_itens += value-used;
            }else{
                left_itens += value;
            }            
        }  
        
        int hasAisle = 0;
        for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
            int value = entry.getValue();
            hasAisle += value;
        }

        if(hasAisle == 0){
            outAisles_.add(aisle);
        }

        if(left_itens == 0){
            return 1;
        }else{
            return 0;
        }
    }

    /*
     * Get the remaining time in seconds
     */
    protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(
                TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS),
                0);
    }

    protected boolean isSolutionFeasible(ChallengeSolution challengeSolution) {
        Set<Integer> selectedOrders = challengeSolution.orders();
        Set<Integer> visitedAisles = challengeSolution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return false;
        }

        int[] totalUnitsPicked = new int[nItems];
        int[] totalUnitsAvailable = new int[nItems];

        // Calculate total units picked
        for (int order : selectedOrders) {
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                totalUnitsPicked[entry.getKey()] += entry.getValue();
            }
        }

        // Calculate total units available
        for (int aisle : visitedAisles) {
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                totalUnitsAvailable[entry.getKey()] += entry.getValue();
            }
        }

        // Check if the total units picked are within bounds
        int totalUnits = Arrays.stream(totalUnitsPicked).sum();
        if (totalUnits < waveSizeLB || totalUnits > waveSizeUB) {
            return false;
        }

        // Check if the units picked do not exceed the units available
        for (int i = 0; i < nItems; i++) {
            if (totalUnitsPicked[i] > totalUnitsAvailable[i]) {
                return false;
            }
        }

        return true;
    }

    protected double computeObjectiveFunction(ChallengeSolution challengeSolution) {
        Set<Integer> selectedOrders = challengeSolution.orders();
        Set<Integer> visitedAisles = challengeSolution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return 0.0;
        }
        int totalUnitsPicked = 0;

        // Calculate total units picked
        for (int order : selectedOrders) {
            totalUnitsPicked += orders.get(order).values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        // Calculate the number of visited aisles
        int numVisitedAisles = visitedAisles.size();

        // Objective function: total units picked / number of visited aisles
        return (double) totalUnitsPicked / numVisitedAisles;
    }
}
