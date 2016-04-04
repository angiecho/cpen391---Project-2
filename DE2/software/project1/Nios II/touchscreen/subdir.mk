################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../touchscreen/touchscreen.c 

OBJS += \
./touchscreen/touchscreen.o 

C_DEPS += \
./touchscreen/touchscreen.d 


# Each subdirectory must supply rules for building sources it contributes
touchscreen/%.o: ../touchscreen/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


