################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../tests/btree_test.c \
../tests/general_tests.c \
../tests/graph_test.c \
../tests/hashmap_test.c 

OBJS += \
./tests/btree_test.o \
./tests/general_tests.o \
./tests/graph_test.o \
./tests/hashmap_test.o 

C_DEPS += \
./tests/btree_test.d \
./tests/general_tests.d \
./tests/graph_test.d \
./tests/hashmap_test.d 


# Each subdirectory must supply rules for building sources it contributes
tests/%.o: ../tests/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


