#!/bin/bash

# Itera de 1 até 20 com padding de zeros (0001 a 0020)
for i in $(seq -f "%04g" 1 20); do
    # Monta o nome dos arquivos
    input_file="datasets/a/instance_${i}.txt"
    output_file="output/instance_${i}.txt"
    
    # Executa o comando com os arquivos gerados
    python3 checker.py "$input_file" "$output_file"
done