################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../misc/rs232/rs232.c 

OBJS += \
./misc/rs232/rs232.o 

C_DEPS += \
./misc/rs232/rs232.d 


# Each subdirectory must supply rules for building sources it contributes
misc/rs232/%.o: ../misc/rs232/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


