################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../pathfinding/a_star.c \
../pathfinding/graph.c 

OBJS += \
./pathfinding/a_star.o \
./pathfinding/graph.o 

C_DEPS += \
./pathfinding/a_star.d \
./pathfinding/graph.d 


# Each subdirectory must supply rules for building sources it contributes
pathfinding/%.o: ../pathfinding/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


