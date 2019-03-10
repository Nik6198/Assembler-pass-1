import java.util.*;
import java.io.*;


class Assembler{
    //pointers
    int litptr;
    int litpoolptr;
    int symptr;

    //tables
    LinkedList<String[]> symtab;
    LinkedList<String[]> littab;
    LinkedList<Integer[]> litpooltab;
    HashMap<String,String[]> opcodetab;
    HashMap<String,String> regtable;

    //file pointers
    BufferedReader br; 
    BufferedWriter bw;

    //buffer
    String buffer;
    
    //constructor to initialise elements
    Assembler(String input,String output){
        symtab = new LinkedList<>();
        littab = new LinkedList<>();
        litpooltab = new LinkedList<>();
        litpooltab.add(new Integer[]{0,0});
        opcodetab = new HashMap<>();
        regtable = new HashMap<>();
        litptr = 0;
        litpoolptr = 0;
        symptr = 0;
        buffer =  null;


        //read opcode table file and store it in hashmap
        try{
            br = new BufferedReader(new FileReader("opcodetab"));
            while ( (buffer = br.readLine())!=null){
                String temp[] = buffer.split(" ");
                opcodetab.put(temp[0],new String[]{temp[1],temp[2],temp[3]});
            }
        }
        catch(IOException exp){
            System.out.println(exp);
        }
        finally{
            try{
                br.close();
            }
            catch(IOException exp){
                System.out.println(exp);
            }
        }

        
        //read register table
        try{
            br = new BufferedReader(new FileReader("register"));
            while ( (buffer = br.readLine())!=null){
                String temp[] = buffer.split(" ");
                regtable.put(temp[0],temp[1]);
            }
        }
        catch(IOException exp){
            System.out.println(exp);
        }
        finally{
            try{
                br.close();
            }
            catch(IOException exp){
                System.out.println(exp);
            }
        }

        
        try{
            br = new BufferedReader(new FileReader(input));
            bw = new BufferedWriter(new FileWriter(output));
        }
        catch(IOException exp){
            System.out.println(exp);
            
        }
    }

    void print(){
        System.out.println("Symbol");
        for(int i = 0 ; i < symtab.size();i++){
            String[] t = symtab.get(i);
            System.out.println( t[0]+" "+t[1]+ " "+t[2]);
        }
        System.out.println("\n\nlit");
        for(int i = 0 ; i < littab.size();i++){
            String[] t = littab.get(i);
            System.out.println( t[0]+" "+t[1]+" "+t[2]);
        }
        System.out.println("\n\nlitpool");
        for(int i = 0 ; i < litpooltab.size();i++){
            Integer[] t = litpooltab.get(i);
            System.out.println( t[0]+" "+t[1]);
        }

    }

    public static boolean isNumeric(String str) { 
        try {  
            Double.parseDouble(str);  
            return true;
        }
        catch(NumberFormatException e){  
            return false;  
        }  
    }

    boolean start(){
        int LC = 0;
        
        try{
            
            while( ! (buffer = br.readLine()).equals("END") ){
            
            
                bw.write(buffer+"\t\t\t\t");
                bw.write(Integer.toString(LC)+" ");
                String[] tokens = buffer.split(" ");
                String label = null;
                String operation = null;
                String operand = null;

                String IC = "";

                if(tokens.length == 3){  //label is present
                    boolean flag = false;
                    for(int i = 0; i < symtab.size();i++){
                        if(tokens[0].equals(symtab.get(i)[0])){
                            flag = true;
                            IC += "(S,"+Integer.toString(i)+") ";
                        }
                    }
                    if(!flag){
                        symtab.add(symptr,new String[]{tokens[0],Integer.toString(LC),"1"});
                        IC += "(S," + Integer.toString(symptr) + ") ";
                    }

                    operation = tokens[1];
                    operand = tokens[2];
                    label = tokens[0];
                    symptr++;
                } 
                else{
                    operation = tokens[0];

                    operand = (operation.equals("LTORG") || operation.equals("STOP")?"":tokens[1]);
                }
                String[] opcode = opcodetab.get(operation);
                IC += "(" + opcode[0] +"," + opcode[1] + ") ";
                switch(operation.trim()){
                    case "STOP" : 
                        LC+=1;
                        bw.write("(IS,00)");
                        break;
                    case "START" :
                        LC = Integer.parseInt(operand);

                        IC += "(C," + operand + ") ";

                        break;

                    case "ORIGIN" :
                        String op = "";
                        int c;
                        if( (c = operand.indexOf('+')) != -1 ) op="\\+";
                        else if( (c = operand.indexOf('-')) != -1) op="-";
                        else op="";

                        if(op.equals("")){
                            if(isNumeric(operand)){ //address is given
                                LC = Integer.parseInt(operand);
                                IC += "(C," + operand + ") ";
                            }
                            else{ // operand is symbol
                                for(int i = 0; i < symtab.size();i++){
                                    if(operand.equals(symtab.get(i)[0])){
                                        LC = Integer.parseInt(symtab.get(i)[1]);
                                        IC += "(S," + Integer.toString(i) + ") ";
                                    }
                                }

                            }
                        }
                        else{
                            String[] ops = operand.split(op);
                            for(int i = 0; i < symtab.size();i++){
                                    if(ops[0].equals(symtab.get(i)[0])){
                                        LC = Integer.parseInt(symtab.get(i)[1]);
                                        IC += "(S," + Integer.toString(i) + ") ";
                                    }


                                }
                                IC += "(C," + ops[1] + ") ";
                                if(op.equals("\\+")) LC += Integer.parseInt(ops[1]);
                                    else if(op.equals("-")) LC-=Integer.parseInt(ops[1]); 
                        }


                        break;

                    case "EQU" :
                        int address = 0;
                         op = "";
                         c=0;
                        if( (c = operand.indexOf('+')) != -1) op="\\+";
                        else if( (c = operand.indexOf('-')) != -1) op="-";
                        else op="";

                        if(op.equals("")){
                            if(isNumeric(operand)){ //address is given
                            address = Integer.parseInt(operand);
                            }
                            else{ // operand is symbol
                                for(int i = 0; i < symtab.size();i++){
                                    if(operand.equals(symtab.get(i)[0])){
                                        address = Integer.parseInt(symtab.get(i)[1]);
                                        IC += "(S," + Integer.toString(i) + ") ";
                                    }
                                }
                            }

                        }
                        else{
                            String[] ops = operand.split(op);

                            for(int i = 0; i < symtab.size();i++){
                                    if(ops[0].equals(symtab.get(i)[0])){
                                        address = Integer.parseInt(symtab.get(i)[1]);
                                        IC += "(S," + Integer.toString(i) + ") ";
                                    }

                                }
                                IC += "(C," + ops[1] + ") ";
                                    if(op.equals("\\+")) address += Integer.parseInt(ops[1]);
                                    else if(op.equals("-")) address+=Integer.parseInt(ops[1]); 


                        }

                        //change the address of symbol
                        for(int i = 0; i < symtab.size();i++){
                            if(label.equals(symtab.get(i)[0])){
                                symtab.set(i,new String[]{label,Integer.toString(address),"1"});
                            }
                        }
                        LC++;
                        break;

                    case "LTORG" : 

                        if(litpooltab.get(litpoolptr)[1] > 0){
                            for(int i = litpooltab.get(litpoolptr)[0];i < litptr; i++){
                                littab.set(i,new String[]{littab.get(i)[0],Integer.toString(LC),"1"});
                                LC++;
                            }
                            litpoolptr++;
                            litpooltab.add(new Integer[]{litptr,0});
                        }
                        break;

                    case "DS" :
                        for(int i = 0; i < symtab.size();i++){
                            if(label.equals(symtab.get(i)[0])){
                                symtab.set(i,new String[]{label,Integer.toString(LC),operand});
                            }
                        }
                        LC = LC + Integer.parseInt(operand);
                        break;

                    case "DC" :
                        for(int i = 0; i < symtab.size();i++){
                            if(label.equals(symtab.get(i)[0])){
                                symtab.set(i,new String[]{label,Integer.toString(LC),"1"});
                            }
                        }
                        IC += "(C," + operand + ") ";
                        LC = LC + Integer.parseInt(operand);
                        break;

                    default : //imperative
                        LC = LC + Integer.parseInt(opcodetab.get(operation)[2]);

                        if(Integer.parseInt(opcode[2]) == 3 ){
                            String[] operands = operand.split(",");
                            if(operands[1].charAt(0)=='='){ //literal
                                if (operands[0].equals("AREG") || operands[0].equals("BREG") || operands[0].equals("CREG") || operands[0].equals("DREG")){
                                    IC += "(R," + regtable.get(operands[0]) + ") ";
                                }
                                else{
                                    for(int i = 0; i < symtab.size();i++){
                                        if(operands[0].equals(symtab.get(i)[0])){
                                            IC += "(S," + Integer.toString(i) + ") ";
                                        }
                                    }
                                    symtab.add(new String[]{operands[0],"","1"});
                                    IC += "(S," + Integer.toString(symptr)+") ";
                                    symptr++;

                                }

                                boolean flag = false;
                                for(int i = litpooltab.get(litpoolptr)[0]; i < litptr ; i++){
                                    if ( operand.equals(littab.get(i)[0])){
                                        flag = true;
                                        IC += "(L," + Integer.toString(i) + ") ";
                                    }
                                }

                                if(litpooltab.get(litpoolptr)[1] == 0 || (!flag)){
                                    littab.add(new String[]{operands[1],"","1"});
                                    IC += "(L," + Integer.toString(litptr) + ") ";
                                    litptr++;
                                    Integer[] temp = litpooltab.get(litpoolptr);
                                    litpooltab.set(litpoolptr,new Integer[]{temp[0],temp[1]+1});
                                }

                            }
                            else{ // symbol
                                int sym = -1;
                                if ((operands[0].equals("AREG") || operands[0].equals("BREG") || operands[0].equals("CREG") || operands[0].equals("DREG") ) && (operands[1].equals("AREG") || operands[1].equals("BREG") || operands[1].equals("CREG") || operands[1].equals("DREG") ) ) {
                                    IC += "(R," + regtable.get(operands[0]) + ") ";
                                    IC += "(R," + regtable.get(operands[1]) + ") ";
                                }
                                else if (operands[0].equals("AREG") || operands[0].equals("BREG") || operands[0].equals("CREG") || operands[0].equals("DREG") ){
                                    sym = 1;
                                }
                                else{
                                    sym = 0;
                                }

                                if(sym != -1){
                                    IC += "(R," + regtable.get(operands[(sym+1)%2]) + ") ";
                                    boolean flag = false;
                                    for(int i = 0; i < symtab.size();i++){
                                        if(operands[sym].equals(symtab.get(i)[0])){
                                            flag = true;
                                            IC += "(S," + Integer.toString(i) + ") ";
                                        }
                                    }
                                    if(!flag){
                                        symtab.add(new String[]{operands[sym],"","1"});
                                        IC += "(S," + Integer.toString(symptr) + ") ";
                                        symptr++;
                                    }
                                }

                            }

                        }
                        else{ //print read
                            if(Integer.parseInt(opcode[2])!=1){
                                boolean flag = false;
                                    for(int i = 0; i < symtab.size();i++){
                                        if(operand.equals(symtab.get(i)[0])){
                                            flag = true;
                                            IC += "(S," + Integer.toString(i) + ") ";
                                        }
                                    }
                                    if(!flag){
                                        symtab.add(new String[]{operand,"","1"});
                                        IC += "(S," + Integer.toString(symptr) + ") ";
                                        symptr++;
                                    }
                            }

                        }

                    break;
                }
            
            
            bw.write(IC);
            bw.newLine();
            bw.flush();

            } 

            //END
            if(litpooltab.get(litpoolptr)[1] > 0){
                for(int i = litpooltab.get(litpoolptr)[0];i < litptr; i++){
                    littab.set(i,new String[]{littab.get(i)[0],Integer.toString(LC),"1"});
                    LC++;
                }
                litpoolptr++;
                litpooltab.add(new Integer[]{litptr,0});
                }   
    
            bw.write("(" + opcodetab.get("END")[0]+ ","+ opcodetab.get("END")[1]+") ");
        }
        catch(IOException exp){
            System.out.println(exp);
            return false;
        }
        print(); //print alll tables
        return true;
    }

}