################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../control/button.c \
../control/control.c \
../control/image.c \
../control/load_node.c 

OBJS += \
./control/button.o \
./control/control.o \
./control/image.o \
./control/load_node.o 

C_DEPS += \
./control/button.d \
./control/control.d \
./control/image.d \
./control/load_node.d 


# Each subdirectory must supply rules for building sources it contributes
control/%.o: ../control/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


